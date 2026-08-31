import type { Purchase } from '../../types/purchase';

export const mockPurchases: Purchase[] = [
  {
    id: 'pu001',
    purchaseNumber: 'PO-001',
    supplier: 'Guangzhou Hair Trading Co.',
    items: [
      { productId: 'p001', productName: 'Body Wave 20"', sku: 'BW20-BLK', quantity: 10, costPerUnit: 180000, totalCost: 1800000 },
      { productId: 'p003', productName: 'Deep Curly 22"', sku: 'DC22-BLK', quantity: 8, costPerUnit: 200000, totalCost: 1600000 },
    ],
    totalCost: 3400000,
    date: '2026-08-15T09:00:00Z',
    notes: 'First shipment — air freight',
    status: 'received',
  },
  {
    id: 'pu002',
    purchaseNumber: 'PO-002',
    supplier: 'Nairobi Beauty Supply',
    items: [
      { productId: 'p002', productName: 'Straight Lace Front 18"', sku: 'STR18-LF', quantity: 5, costPerUnit: 155000, totalCost: 775000 },
    ],
    totalCost: 775000,
    date: '2026-08-18T10:00:00Z',
    status: 'received',
  },
  {
    id: 'pu003',
    purchaseNumber: 'PO-003',
    supplier: 'Guangzhou Hair Trading Co.',
    items: [
      { productId: 'p005', productName: 'Water Wave 24"', sku: 'WW24-BRN', quantity: 6, costPerUnit: 235000, totalCost: 1410000 },
    ],
    totalCost: 1410000,
    date: '2026-08-22T09:00:00Z',
    status: 'received',
  },
  {
    id: 'pu004',
    purchaseNumber: 'PO-004',
    supplier: 'Ali Express Direct',
    items: [
      { productId: 'p007', productName: 'Afro Kinky 16"', sku: 'AK16-BLK', quantity: 4, costPerUnit: 130000, totalCost: 520000 },
    ],
    totalCost: 520000,
    date: '2026-08-26T10:00:00Z',
    status: 'received',
  },
  {
    id: 'pu005',
    purchaseNumber: 'PO-005',
    supplier: 'Guangzhou Hair Trading Co.',
    items: [
      { productId: 'p009', productName: 'Body Wave 26"', sku: 'BW26-BLK', quantity: 5, costPerUnit: 250000, totalCost: 1250000 },
      { productId: 'p008', productName: 'Straight Bob 12"', sku: 'SB12-BLK', quantity: 15, costPerUnit: 95000, totalCost: 1425000 },
    ],
    totalCost: 2675000,
    date: '2026-08-29T09:00:00Z',
    status: 'received',
  },
];
