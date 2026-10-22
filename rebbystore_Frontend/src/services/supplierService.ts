import { apiClient } from './apiClient';

export interface Supplier {
  id: string;
  name: string;
  phone: string | null;
  email: string | null;
  address: string | null;
  createdAt: string;
  updatedAt: string;
}

interface BackendSupplier {
  id: number;
  name: string;
  phone: string | null;
  email: string | null;
  address: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateSupplierInput {
  name: string;
  phone?: string;
  email?: string;
  address?: string;
}

function mapSupplier(supplier: BackendSupplier): Supplier {
  return {
    id: String(supplier.id),
    name: supplier.name,
    phone: supplier.phone,
    email: supplier.email,
    address: supplier.address,
    createdAt: supplier.createdAt,
    updatedAt: supplier.updatedAt,
  };
}

export const supplierService = {
  async getAll(): Promise<Supplier[]> {
    const response = await apiClient<BackendSupplier[]>('/api/suppliers');
    return response.map(mapSupplier);
  },

  async getById(id: string): Promise<Supplier> {
    const response = await apiClient<BackendSupplier>(`/api/suppliers/${Number(id)}`);
    return mapSupplier(response);
  },

  async create(data: CreateSupplierInput): Promise<Supplier> {
    const response = await apiClient<BackendSupplier>('/api/suppliers', {
      method: 'POST',
      body: JSON.stringify({
        name: data.name.trim(),
        phone: data.phone?.trim() || null,
        email: data.email?.trim() || null,
        address: data.address?.trim() || null,
      }),
    });

    return mapSupplier(response);
  },
};
