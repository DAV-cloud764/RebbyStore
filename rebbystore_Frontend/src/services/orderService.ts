import type { Order, OrderItem, OrderStatus } from '../types/order';
import { ApiError, apiClient } from './apiClient';

interface BackendOrder {
  id: number;
  customerId: number;
  orderNumber: string;
  customerName: string;
  customerPhone: string;
  customerEmail: string;
  deliveryAddress: string;
  deliveryCity: string;
  deliveryRegion: string;
  deliveryNotes: string | null;
  paymentMethod: 'CASH_ON_DELIVERY';
  subtotal: number;
  deliveryFee: number;
  total: number;
  status:
    | 'PENDING'
    | 'CONFIRMED'
    | 'PROCESSING'
    | 'READY_FOR_DELIVERY'
    | 'DELIVERED'
    | 'CANCELLED';
  expiresAt: string;
  createdAt: string;
  updatedAt: string;
}

interface BackendOrderItem {
  id: number;
  productId: number;
  productName: string;
  sku: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

interface GuestOrderConfirmationResponse {
  order: BackendOrder;
  items: BackendOrderItem[];
}

interface GuestCheckoutResponse {
  confirmationToken: string;
  confirmation: GuestOrderConfirmationResponse;
}

interface CheckoutCustomerInput {
  fullName: string;
  phone: string;
  email: string;
}

interface CheckoutDeliveryInput {
  address: string;
  city: string;
  region: string;
  notes?: string;
}

interface CheckoutItemInput {
  product: {
    id: string;
  };
  quantity: number;
}

export interface CheckoutOrderInput {
  customer: CheckoutCustomerInput;
  delivery: CheckoutDeliveryInput;
  items: CheckoutItemInput[];
  paymentMethod: 'cash-on-delivery';
}

const BACKEND_STATUSES = [
  'PENDING',
  'CONFIRMED',
  'PROCESSING',
  'READY_FOR_DELIVERY',
  'DELIVERED',
  'CANCELLED',
] as const;

const GUEST_CONFIRMATION_TOKEN_PREFIX =
  'rebbystore_guest_confirmation_token:';

function guestConfirmationTokenKey(
  orderId: string | number
): string {
  return `${GUEST_CONFIRMATION_TOKEN_PREFIX}${orderId}`;
}

function mapOrderStatus(
  status: BackendOrder['status']
): OrderStatus {
  switch (status) {
    case 'READY_FOR_DELIVERY':
      return 'ready-for-delivery';
    case 'CONFIRMED':
      return 'confirmed';
    case 'PROCESSING':
      return 'processing';
    case 'DELIVERED':
      return 'delivered';
    case 'CANCELLED':
      return 'cancelled';
    default:
      return 'pending';
  }
}

function mapPaymentMethod(): Order['paymentMethod'] {
  return 'cash-on-delivery';
}

function mapOrder(
  order: BackendOrder,
  items: BackendOrderItem[]
): Order {
  const mappedItems: OrderItem[] = items.map((item) => ({
    id: String(item.id),
    productId: String(item.productId),
    productName: item.productName,
    sku: item.sku,
    quantity: item.quantity,
    unitPrice: Number(item.unitPrice),
    subtotal: Number(item.subtotal),
  }));

  return {
    id: String(order.id),
    orderNumber: order.orderNumber,

    customer: {
      id: String(order.customerId),
      fullName: order.customerName,
      phone: order.customerPhone,
      email: order.customerEmail,
    },

    delivery: {
      address: order.deliveryAddress,
      city: order.deliveryCity,
      region: order.deliveryRegion,
      notes: order.deliveryNotes ?? undefined,
    },

    items: mappedItems,

    subtotal: Number(order.subtotal),
    deliveryFee: Number(order.deliveryFee),
    total: Number(order.total),

    status: mapOrderStatus(order.status),
    paymentMethod: mapPaymentMethod(),

    expiresAt: order.expiresAt,
    createdAt: order.createdAt,
    updatedAt: order.updatedAt,
  };
}

async function getItems(orderId: string): Promise<BackendOrderItem[]> {
  return apiClient<BackendOrderItem[]>(
    `/api/orders/${orderId}/items`
  );
}


function statusEndpoint(status: OrderStatus): string {
  switch (status) {
    case 'pending':
      return '/api/orders/{id}';
    case 'confirmed':
      return '/api/orders/{id}/confirm';
    case 'processing':
      return '/api/orders/{id}/process';
    case 'ready-for-delivery':
      return '/api/orders/{id}/ready-for-delivery';
    case 'delivered':
      return '/api/orders/{id}/deliver';
    case 'cancelled':
      return '/api/orders/{id}/cancel';
  }
}

export const orderService = {
  async getAll(): Promise<Order[]> {
    const responses = await Promise.all(
      BACKEND_STATUSES.map((status) =>
        apiClient<BackendOrder[]>(
          `/api/orders/status/${status}`
        )
      )
    );

    const orders = responses
      .flat()
      .map((order) => ({ order, items: [] as BackendOrderItem[] }));

    const hydrated = await Promise.all(
      orders.map(async ({ order }) => {
        const items = await getItems(String(order.id));
        return mapOrder(order, items);
      })
    );

    return hydrated.sort(
      (a, b) =>
        new Date(b.createdAt).getTime() -
        new Date(a.createdAt).getTime()
    );
  },

async getById(id: string): Promise<Order | undefined> {
  const tokenKey = guestConfirmationTokenKey(id);
  const guestToken = sessionStorage.getItem(tokenKey);

  if (guestToken) {
    try {
      const confirmation =
        await apiClient<GuestOrderConfirmationResponse>(
          `/api/checkout/confirmation/${encodeURIComponent(guestToken)}`,
          { authenticated: false }
        );

      return mapOrder(
        confirmation.order,
        confirmation.items
      );
    } catch (error) {
      if (error instanceof ApiError && error.status === 404) {
        sessionStorage.removeItem(tokenKey);
        return undefined;
      }

      throw error;
    }
  }

  try {
    const backendOrder = await apiClient<BackendOrder>(
      `/api/orders/${id}`
    );

    const items = await getItems(id);

    return mapOrder(backendOrder, items);
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) {
      return undefined;
    }

    throw error;
  }
},

  async getByStatus(status: OrderStatus): Promise<Order[]> {
    const backendStatus =
      status === 'ready-for-delivery'
        ? 'READY_FOR_DELIVERY'
        : status.toUpperCase();

    const backendOrders = await apiClient<BackendOrder[]>(
      `/api/orders/status/${backendStatus}`
    );

    return Promise.all(
      backendOrders.map(async (order) => {
        const items = await getItems(String(order.id));
        return mapOrder(order, items);
      })
    );
  },

  async create(data: CheckoutOrderInput): Promise<Order> {
  if (data.items.length === 0) {
    throw new Error('Cannot create an order with no items');
  }

  const result = await apiClient<GuestCheckoutResponse>(
    '/api/checkout',
    {
      method: 'POST',
      authenticated: false,
      body: JSON.stringify({
        customer: {
          fullName: data.customer.fullName.trim(),
          phone: data.customer.phone.trim(),
          email: data.customer.email.trim(),
        },
        delivery: {
          address: data.delivery.address.trim(),
          city: data.delivery.city.trim(),
          region: data.delivery.region.trim(),
          notes: data.delivery.notes?.trim() || null,
        },
        items: data.items.map((item) => ({
          productId: Number(item.product.id),
          quantity: item.quantity,
        })),
      }),
    }
  );

  const orderId = String(result.confirmation.order.id);

  sessionStorage.setItem(
    guestConfirmationTokenKey(orderId),
    result.confirmationToken
  );

  return mapOrder(
    result.confirmation.order,
    result.confirmation.items
  );
},

  async updateStatus(
    id: string,
    status: OrderStatus
  ): Promise<Order> {
    if (status === 'pending') {
      const existing = await this.getById(id);

      if (!existing) {
        throw new Error('Order not found');
      }

      return existing;
    }

    const endpoint = statusEndpoint(status).replace(
      '{id}',
      id
    );

    const updatedOrder = await apiClient<BackendOrder>(
      endpoint,
      {
        method: 'POST',
      }
    );

    const items = await getItems(id);

    return mapOrder(updatedOrder, items);
  },
};
