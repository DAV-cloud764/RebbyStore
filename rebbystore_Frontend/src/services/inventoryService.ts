import { apiClient } from './apiClient';
import type { InventoryMovement } from '../types/inventory';

interface BackendInventoryMovement {
  id: number;
  productId: number;
  productName: string;
  sku: string;
  orderId: number | null;
  purchaseId: number | null;
  type: 'in' | 'out';
  quantity: number;
  reason: string;
  createdAt: string;
}

interface BackendStockResponse {
  productId: number;
  stock: number;
}

interface BackendLowStockResponse {
  productId: number;
  lowStock: boolean;
}

function mapMovement(
  movement: BackendInventoryMovement,
): InventoryMovement {
  return {
    id: String(movement.id),
    productId: String(movement.productId),
    productName: movement.productName,
    sku: movement.sku,
    type: movement.type,
    quantity: movement.quantity,
    reason: movement.reason as InventoryMovement['reason'],
    date: movement.createdAt,
    orderId: movement.orderId,
    purchaseId: movement.purchaseId,
  };
}

async function getProductMovements(
  productId: string,
): Promise<InventoryMovement[]> {
  const response = await apiClient<BackendInventoryMovement[]>(
    `/api/inventory/${Number(productId)}/movements`,
  );

  return response.map(mapMovement);
}

export const inventoryService = {
  async stockIn(
    productId: string,
    quantity: number,
  ): Promise<InventoryMovement> {
    const response = await apiClient<BackendInventoryMovement>(
      `/api/inventory/${Number(productId)}/stock-in`,
      {
        method: 'POST',
        body: JSON.stringify({ quantity }),
      },
    );

    return mapMovement(response);
  },

  async stockOut(
    productId: string,
    quantity: number,
  ): Promise<InventoryMovement> {
    const response = await apiClient<BackendInventoryMovement>(
      `/api/inventory/${Number(productId)}/stock-out`,
      {
        method: 'POST',
        body: JSON.stringify({ quantity }),
      },
    );

    return mapMovement(response);
  },

  async adjustStock(
    productId: string,
    quantity: number,
    increase: boolean,
  ): Promise<InventoryMovement> {
    const response = await apiClient<BackendInventoryMovement>(
      `/api/inventory/${Number(productId)}/adjust`,
      {
        method: 'POST',
        body: JSON.stringify({
          quantity,
          increase,
        }),
      },
    );

    return mapMovement(response);
  },

  async getCurrentStock(productId: string): Promise<number> {
    const response = await apiClient<BackendStockResponse>(
      `/api/inventory/${Number(productId)}/stock`,
    );

    return response.stock;
  },

  async isLowStock(productId: string): Promise<boolean> {
    const response = await apiClient<BackendLowStockResponse>(
      `/api/inventory/${Number(productId)}/low-stock`,
    );

    return response.lowStock;
  },

  async getProductMovements(
    productId: string,
  ): Promise<InventoryMovement[]> {
    return getProductMovements(productId);
  },

  async getMovements(): Promise<InventoryMovement[]> {
    return [];
  },
};