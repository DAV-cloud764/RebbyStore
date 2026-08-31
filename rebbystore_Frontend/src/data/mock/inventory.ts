import type { InventoryMovement } from '../../types/inventory';

export const mockInventoryMovements: InventoryMovement[] = [
  { id: 'im001', productId: 'p001', productName: 'Body Wave 20"', sku: 'BW20-BLK', type: 'in', quantity: 10, reason: 'purchase', reference: 'PO-001', date: '2026-08-15T09:00:00Z' },
  { id: 'im002', productId: 'p003', productName: 'Deep Curly 22"', sku: 'DC22-BLK', type: 'in', quantity: 8, reason: 'purchase', reference: 'PO-001', date: '2026-08-15T09:00:00Z' },
  { id: 'im003', productId: 'p002', productName: 'Straight Lace Front 18"', sku: 'STR18-LF', type: 'in', quantity: 5, reason: 'purchase', reference: 'PO-002', date: '2026-08-18T10:00:00Z' },
  { id: 'im004', productId: 'p001', productName: 'Body Wave 20"', sku: 'BW20-BLK', type: 'out', quantity: 2, reason: 'sale', reference: 'RS-001423', date: '2026-08-20T14:00:00Z' },
  { id: 'im005', productId: 'p004', productName: 'Yaki Bob 14"', sku: 'YB14-BLK', type: 'out', quantity: 3, reason: 'damaged', notes: 'Damaged in transit', date: '2026-08-21T11:00:00Z' },
  { id: 'im006', productId: 'p005', productName: 'Water Wave 24"', sku: 'WW24-BRN', type: 'in', quantity: 6, reason: 'purchase', reference: 'PO-003', date: '2026-08-22T09:00:00Z' },
  { id: 'im007', productId: 'p003', productName: 'Deep Curly 22"', sku: 'DC22-BLK', type: 'out', quantity: 1, reason: 'sale', reference: 'RS-001424', date: '2026-08-25T12:00:00Z' },
  { id: 'im008', productId: 'p007', productName: 'Afro Kinky 16"', sku: 'AK16-BLK', type: 'in', quantity: 4, reason: 'purchase', reference: 'PO-004', date: '2026-08-26T10:00:00Z' },
  { id: 'im009', productId: 'p002', productName: 'Straight Lace Front 18"', sku: 'STR18-LF', type: 'out', quantity: 1, reason: 'sale', reference: 'RS-001428', date: '2026-08-28T10:00:00Z' },
  { id: 'im010', productId: 'p009', productName: 'Body Wave 26"', sku: 'BW26-BLK', type: 'in', quantity: 5, reason: 'purchase', reference: 'PO-005', date: '2026-08-29T09:00:00Z' },
  { id: 'im011', productId: 'p007', productName: 'Afro Kinky 16"', sku: 'AK16-BLK', type: 'out', quantity: 3, reason: 'sale', reference: 'RS-001428', date: '2026-08-29T10:00:00Z' },
  { id: 'im012', productId: 'p008', productName: 'Straight Bob 12"', sku: 'SB12-BLK', type: 'in', quantity: 15, reason: 'purchase', reference: 'PO-005', date: '2026-08-29T09:00:00Z' },
];
