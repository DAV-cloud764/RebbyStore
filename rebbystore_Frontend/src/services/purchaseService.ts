import { apiClient } from './apiClient';
import type {
  Purchase,
  PurchaseItem,
  PurchaseStatus,
} from '../types/purchase';

interface BackendPurchase {
  id: number;
  supplierId: number;
  supplierName: string;
  purchaseNumber: string;
  status: PurchaseStatus;
  purchaseDate: string;
  totalCost: number;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

interface BackendPurchaseItem {
  id: number;
  productId: number;
  productName: string;
  sku: string;
  quantity: number;
  unitCost: number;
  subtotal: number;
}

interface CreatePurchaseInput {
  supplierId: string;
  purchaseDate: string;
  notes?: string;
}

interface AddPurchaseItemInput {
  productId: string;
  quantity: number;
  unitCost: number;
}

interface CreateAndReceivePurchaseInput {
  supplierId: string;
  purchaseDate: string;
  notes?: string;
  items: AddPurchaseItemInput[];
}

function mapPurchase(
  purchase: BackendPurchase,
  items: PurchaseItem[] = [],
): Purchase {
  return {
    id: String(purchase.id),
    supplierId: String(purchase.supplierId),
    supplier: purchase.supplierName,
    purchaseNumber: purchase.purchaseNumber,
    items,
    totalCost: Number(purchase.totalCost),
    date: purchase.purchaseDate,
    notes: purchase.notes ?? undefined,
    status: purchase.status,
  };
}

function mapPurchaseItem(
  item: BackendPurchaseItem,
): PurchaseItem {
  return {
    id: String(item.id),
    productId: String(item.productId),
    productName: item.productName,
    sku: item.sku,
    quantity: item.quantity,
    costPerUnit: Number(item.unitCost),
    totalCost: Number(item.subtotal),
  };
}

async function getItems(purchaseId: string): Promise<PurchaseItem[]> {
  const response = await apiClient<BackendPurchaseItem[]>(
    `/api/purchases/${Number(purchaseId)}/items`,
  );

  return response.map(mapPurchaseItem);
}

async function getPurchasesByStatus(
  status: PurchaseStatus,
): Promise<BackendPurchase[]> {
  return apiClient<BackendPurchase[]>(
    `/api/purchases/status/${status}`,
  );
}

export const purchaseService = {
  async getAll(): Promise<Purchase[]> {
    const statuses: PurchaseStatus[] = [
      'draft',
      'ordered',
      'received',
      'cancelled',
    ];

    const groups = await Promise.all(
      statuses.map((status) => getPurchasesByStatus(status)),
    );

    const purchases = groups.flat();

    const mapped = await Promise.all(
      purchases.map(async (purchase) => {
        const items = await getItems(String(purchase.id));

        return mapPurchase(purchase, items);
      }),
    );

    return mapped.sort(
      (a, b) =>
        new Date(b.date).getTime() -
        new Date(a.date).getTime(),
    );
  },

  async getById(id: string): Promise<Purchase> {
    const purchase = await apiClient<BackendPurchase>(
      `/api/purchases/${Number(id)}`,
    );

    const items = await getItems(id);

    return mapPurchase(purchase, items);
  },

  async create(
    data: CreatePurchaseInput,
  ): Promise<Purchase> {
    const response = await apiClient<BackendPurchase>(
      '/api/purchases',
      {
        method: 'POST',
        body: JSON.stringify({
          supplierId: Number(data.supplierId),
          purchaseDate: data.purchaseDate,
          notes: data.notes || null,
        }),
      },
    );

    return mapPurchase(response);
  },

  async addItem(
    purchaseId: string,
    data: AddPurchaseItemInput,
  ): Promise<PurchaseItem> {
    const response = await apiClient<BackendPurchaseItem>(
      `/api/purchases/${Number(purchaseId)}/items`,
      {
        method: 'POST',
        body: JSON.stringify({
          productId: Number(data.productId),
          quantity: data.quantity,
          unitCost: data.unitCost,
        }),
      },
    );

    return mapPurchaseItem(response);
  },

  async order(id: string): Promise<Purchase> {
    const response = await apiClient<BackendPurchase>(
      `/api/purchases/${Number(id)}/order`,
      {
        method: 'POST',
      },
    );

    const items = await getItems(id);

    return mapPurchase(response, items);
  },

  async receive(id: string): Promise<Purchase> {
    const response = await apiClient<BackendPurchase>(
      `/api/purchases/${Number(id)}/receive`,
      {
        method: 'POST',
      },
    );

    const items = await getItems(id);

    return mapPurchase(response, items);
  },

  async cancel(id: string): Promise<Purchase> {
    const response = await apiClient<BackendPurchase>(
      `/api/purchases/${Number(id)}/cancel`,
      {
        method: 'POST',
      },
    );

    const items = await getItems(id);

    return mapPurchase(response, items);
  },

  async createAndReceive(
    data: CreateAndReceivePurchaseInput,
  ): Promise<Purchase> {
    const purchase = await this.create({
      supplierId: data.supplierId,
      purchaseDate: data.purchaseDate,
      notes: data.notes,
    });

    for (const item of data.items) {
      await this.addItem(purchase.id, item);
    }

    await this.order(purchase.id);

    return this.receive(purchase.id);
  },

  async getTotalThisMonth(): Promise<number> {
    const purchases = await this.getAll();

    const now = new Date();

    const monthStart = new Date(
      now.getFullYear(),
      now.getMonth(),
      1,
    );

    return purchases
      .filter(
        (purchase) =>
          purchase.status === 'received' &&
          new Date(purchase.date) >= monthStart,
      )
      .reduce(
        (sum, purchase) => sum + purchase.totalCost,
        0,
      );
  },
};