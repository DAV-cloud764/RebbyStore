import type { Product } from './product';

export type OrderStatus =
  | 'pending'
  |  'confirmed'
  |  'processing'
  |  'ready-for-delivery'
  |  'delivered'
  |  'cancelled';

export interface OrderItem {
  id?: string;
  productId: string;
  productName: string;
  sku: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;

  // Available when an order is built from the storefront cart
  // or when the full product has been resolved.
  product?: Product;
}

export interface CustomerInfo {
  id?: string;
  fullName: string;
  phone: string;
  email: string;
}

export interface DeliveryInfo {
  address: string;
  city: string;
  region: string;
  notes?: string;
}

export interface Order {
  id: string;
  orderNumber: string;
  customer: CustomerInfo;
  delivery: DeliveryInfo;
  items: OrderItem[];
  subtotal: number;
  deliveryFee: number;
  total: number;
  paymentMethod: 'cash-on-delivery';
  status: OrderStatus;
  expiresAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CartItem {
  product: Product;
  quantity: number;
}