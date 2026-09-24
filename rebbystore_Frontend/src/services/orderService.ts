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

interface BackendCustomer {
  id: number;
  fullName: string;
  phone: string;
  email: string;
  totalOrders: number;
  totalSpent: number;
  createdAt: string;
  updatedAt: string;
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

async function findCustomerByPhone(
  phone: string
): Promise<BackendCustomer | undefined> {
  try {
    return await apiClient<BackendCustomer>(
      `/api/customers/phone/${encodeURIComponent(phone)}`
    );
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) {
      return undefined;
    }
    throw error;
  }
}

async function findCustomerByEmail(
  email: string
): Promise<BackendCustomer | undefined> {
  try {
    return await apiClient<BackendCustomer>(
      `/api/customers/email/${encodeURIComponent(email)}`
    );
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) {
      return undefined;
    }
    throw error;
  }
}

async function resolveCustomer(
  customerInput: CheckoutCustomerInput
): Promise<BackendCustomer> {
  const existingByPhone = await findCustomerByPhone(
    customerInput.phone
  );

  if (existingByPhone) {
    return existingByPhone;
  }

  const existingByEmail = await findCustomerByEmail(
    customerInput.email
  );

  if (existingByEmail) {
    return existingByEmail;
  }

  return apiClient<BackendCustomer>('/api/customers', {
    method: 'POST',
    body: JSON.stringify({
      fullName: customerInput.fullName.trim(),
      phone: customerInput.phone.replace(/\s/g, ''),
      email: customerInput.email.trim(),
    }),
  });
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

    const customer = await resolveCustomer(data.customer);

    const backendOrder = await apiClient<BackendOrder>(
      '/api/orders',
      {
        method: 'POST',
        body: JSON.stringify({
          customerId: customer.id,
          deliveryAddress: data.delivery.address.trim(),
          deliveryCity: data.delivery.city.trim(),
          deliveryRegion: data.delivery.region.trim(),
          deliveryNotes: data.delivery.notes?.trim() || null,
          paymentMethod: 'CASH_ON_DELIVERY',
        }),
      }
    );

    try {
      for (const item of data.items) {
        await apiClient<BackendOrderItem>(
          `/api/orders/${backendOrder.id}/items`,
          {
            method: 'POST',
            body: JSON.stringify({
              productId: Number(item.product.id),
              quantity: item.quantity,
            }),
          }
        );
      }
    } catch (error) {
      // Clean up the incomplete pending order when possible.
      try {
        await apiClient(
          `/api/orders/${backendOrder.id}/cancel`,
          {
            method: 'POST',
          }
        );
      } catch {
        // Preserve the original item/order error.
      }

      throw error;
    }

    const finalOrder = await apiClient<BackendOrder>(
      `/api/orders/${backendOrder.id}`
    );

    const items = await getItems(String(finalOrder.id));

    return mapOrder(finalOrder, items);
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