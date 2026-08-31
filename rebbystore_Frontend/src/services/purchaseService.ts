import type { Purchase } from '../types/purchase';
import { mockPurchases } from '../data/mock/purchases';

let purchases: Purchase[] = [...mockPurchases];

export const purchaseService = {
  getAll(): Promise<Purchase[]> {
    return Promise.resolve([...purchases].sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime()));
  },

  create(data: Omit<Purchase, 'id'>): Promise<Purchase> {
    const newPurchase: Purchase = { ...data, id: `pu${Date.now()}` };
    purchases = [newPurchase, ...purchases];
    return Promise.resolve(newPurchase);
  },

  getTotalThisMonth(): Promise<number> {
    const now = new Date();
    const monthStart = new Date(now.getFullYear(), now.getMonth(), 1);
    const total = purchases
      .filter((p) => new Date(p.date) >= monthStart)
      .reduce((sum, p) => sum + p.totalCost, 0);
    return Promise.resolve(total);
  },
};
