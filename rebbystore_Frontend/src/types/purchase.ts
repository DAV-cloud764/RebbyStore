export type PurchaseStatus =
  | 'draft'
  | 'ordered'
  | 'received'
  | 'cancelled';

export interface PurchaseItem {
  id?: string;
  productId: string;
  productName: string;
  sku: string;
  quantity: number;
  costPerUnit: number;
  totalCost: number;
}

export interface Purchase {
  id: string;
  supplierId: string;
  supplier: string;
  purchaseNumber: string;
  items: PurchaseItem[];
  totalCost: number;
  date: string;
  notes?: string;
  status: PurchaseStatus;
}