import { apiClient } from './apiClient';
import type { Customer } from '../types/customer';

interface BackendCustomer {
  id: number;
  fullName: string;
  phone: string;
  email: string;
  totalOrders: number;
  totalSpent: number;
  createdAt: string;
  updatedAt: string;
}

function mapCustomer(customer: BackendCustomer): Customer {
  return {
    id: String(customer.id),
    fullName: customer.fullName,
    phone: customer.phone,
    email: customer.email,
    totalOrders: customer.totalOrders,
    totalSpent: Number(customer.totalSpent),
    createdAt: customer.createdAt,

    // These fields are not currently exposed by CustomerResponse.
    address: undefined,
    city: undefined,
    region: undefined,
    lastOrderDate: undefined,
  };
}

export const customerService = {
  async getAll(): Promise<Customer[]> {
    const customers = await apiClient<BackendCustomer[]>(
      '/api/customers'
    );

    return customers.map(mapCustomer);
  },

  async getById(id: string): Promise<Customer> {
    const customer = await apiClient<BackendCustomer>(
      `/api/customers/${id}`
    );

    return mapCustomer(customer);
  },
};