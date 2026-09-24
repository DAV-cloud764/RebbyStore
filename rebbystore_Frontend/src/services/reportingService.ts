import { apiClient } from './apiClient';

export interface SalesSummary {
  deliveredCount: number;
  totalRevenue: number;
  averageOrderValue: number;
}

export interface OrderSummary {
  pending: number;
  confirmed: number;
  processing: number;
  readyForDelivery: number;
  delivered: number;
  cancelled: number;
  total: number;
}

export interface PurchaseSummary {
  totalPurchases: number;
  receivedPurchases: number;
  totalPurchaseCost: number;
}

export interface InventorySummary {
  totalProducts: number;
  totalUnitsInStock: number;
  lowStockProducts: number;
}

export interface CustomerSummary {
  totalCustomers: number;
  totalOrders: number;
  totalSpent: number;
}

export const reportingService = {
  getSalesSummary(): Promise<SalesSummary> {
    return apiClient<SalesSummary>('/api/reporting/sales');
  },

  getOrderSummary(): Promise<OrderSummary> {
    return apiClient<OrderSummary>('/api/reporting/orders');
  },

  getPurchaseSummary(): Promise<PurchaseSummary> {
    return apiClient<PurchaseSummary>('/api/reporting/purchases');
  },

  getInventorySummary(): Promise<InventorySummary> {
    return apiClient<InventorySummary>('/api/reporting/inventory');
  },

  getCustomerSummary(): Promise<CustomerSummary> {
    return apiClient<CustomerSummary>('/api/reporting/customers');
  },
};