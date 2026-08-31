// productService — replace mock implementations with real API calls when backend is ready
import type { Product } from '../types/product';
import { mockProducts } from '../data/mock/products';

let products: Product[] = [...mockProducts];

export const productService = {
  getAll(): Promise<Product[]> {
    return Promise.resolve([...products]);
  },

  getById(id: string): Promise<Product | undefined> {
    return Promise.resolve(products.find((p) => p.id === id));
  },

  getFeatured(): Promise<Product[]> {
    return Promise.resolve(products.filter((p) => p.isFeatured && p.status === 'active'));
  },

  getNewArrivals(): Promise<Product[]> {
    return Promise.resolve(products.filter((p) => p.isNew && p.status === 'active'));
  },

  create(data: Omit<Product, 'id'>): Promise<Product> {
    const newProduct: Product = { ...data, id: `p${Date.now()}` };
    products = [...products, newProduct];
    return Promise.resolve(newProduct);
  },

  update(id: string, data: Partial<Product>): Promise<Product> {
    products = products.map((p) => (p.id === id ? { ...p, ...data } : p));
    const updated = products.find((p) => p.id === id);
    if (!updated) return Promise.reject(new Error('Product not found'));
    return Promise.resolve(updated);
  },

  delete(id: string): Promise<void> {
    products = products.filter((p) => p.id !== id);
    return Promise.resolve();
  },
};
