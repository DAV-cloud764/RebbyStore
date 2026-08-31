export type MovementType = 'in' | 'out';
export type MovementReason =
  | 'purchase'
  | 'sale'
  | 'damaged'
  | 'lost'
  | 'returned-to-supplier'
  | 'adjustment';

export interface InventoryRecord {
  id: string;
  productId: string;
  productName: string;
  sku: string;
  currentStock: number;
  lowStockThreshold: number;
  totalIn: number;
  totalOut: number;
}

export interface InventoryMovement {
  id: string;
  productId: string;
  productName: string;
  sku: string;
  type: MovementType;
  quantity: number;
  reason: MovementReason;
  reference?: string;
  notes?: string;
  date: string;
  performedBy?: string;
}
