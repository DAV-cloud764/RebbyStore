import type { InventoryMovement } from '../types/inventory';
import { mockInventoryMovements } from '../data/mock/inventory';
import { mockProducts } from '../data/mock/products';
import { productService } from './productService';

let movements: InventoryMovement[] = [...mockInventoryMovements];

export const inventoryService = {
  getMovements(): Promise<InventoryMovement[]> {
    return Promise.resolve([...movements].sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime()));
  },

  async recordMovement(data: Omit<InventoryMovement, 'id'>): Promise<InventoryMovement> {
    const movement: InventoryMovement = { ...data, id: `im${Date.now()}` };
    movements = [movement, ...movements];

    // Update product stock quantity (local state demonstration)
    const product = mockProducts.find((p) => p.id === data.productId);
    if (product) {
      const delta = data.type === 'in' ? data.quantity : -data.quantity;
      await productService.update(data.productId, {
        stockQuantity: Math.max(0, product.stockQuantity + delta),
      });
    }
    return Promise.resolve(movement);
  },

  getStockSummary(): Promise<{ totalIn: number; totalOut: number }> {
    const totalIn = movements.filter((m) => m.type === 'in').reduce((sum, m) => sum + m.quantity, 0);
    const totalOut = movements.filter((m) => m.type === 'out').reduce((sum, m) => sum + m.quantity, 0);
    return Promise.resolve({ totalIn, totalOut });
  },
};
