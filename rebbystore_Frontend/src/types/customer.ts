export interface Customer {
  id: string;
  fullName: string;
  phone: string;
  email: string;
  address?: string;
  city?: string;
  region?: string;
  totalOrders: number;
  totalSpent: number;
  lastOrderDate?: string;
  createdAt: string;
}
