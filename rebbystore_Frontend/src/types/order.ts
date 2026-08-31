import type { Product } from './product';

export type OrderStatus =
  | 'pending'
  | 'confirmed'
  | 'processing'
  | 'ready-for-delivery'
  | 'delivered'
  | 'cancelled';

export interface OrderItem {
  product: Product;
  quantity: number;
  unitPrice: number;
}

export interface CustomerInfo {
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
  createdAt: string;
  updatedAt: string;
}

export interface CartItem {
  product: Product;
  quantity: number;
}
