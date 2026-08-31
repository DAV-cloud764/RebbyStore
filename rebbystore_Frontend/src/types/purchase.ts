export interface PurchaseItem {
  productId: string;
  productName: string;
  sku: string;
  quantity: number;
  costPerUnit: number;
  totalCost: number;
}

export interface Purchase {
  id: string;
  purchaseNumber: string;
  supplier: string;
  items: PurchaseItem[];
  totalCost: number;
  date: string;
  notes?: string;
  status: 'pending' | 'received' | 'partial';
}
