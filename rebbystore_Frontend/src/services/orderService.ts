import type { Order, OrderStatus } from '../types/order';
import { mockOrders } from '../data/mock/orders';
import { generateId, generateOrderNumber } from '../utils/formatting';

let orders: Order[] = [...mockOrders];

export const orderService = {
  getAll(): Promise<Order[]> {
    return Promise.resolve([...orders].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()));
  },

  getById(id: string): Promise<Order | undefined> {
    return Promise.resolve(orders.find((o) => o.id === id));
  },

  create(data: Omit<Order, 'id' | 'orderNumber' | 'status' | 'createdAt' | 'updatedAt'>): Promise<Order> {
    const now = new Date().toISOString();
    const newOrder: Order = {
      ...data,
      id: generateId(),
      orderNumber: generateOrderNumber(),
      status: 'pending',
      createdAt: now,
      updatedAt: now,
    };
    orders = [newOrder, ...orders];
    return Promise.resolve(newOrder);
  },

  updateStatus(id: string, status: OrderStatus): Promise<Order> {
    orders = orders.map((o) =>
      o.id === id ? { ...o, status, updatedAt: new Date().toISOString() } : o
    );
    const updated = orders.find((o) => o.id === id);
    if (!updated) return Promise.reject(new Error('Order not found'));
    return Promise.resolve(updated);
  },
};
