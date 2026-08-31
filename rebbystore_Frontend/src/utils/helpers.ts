import { getStockStatus } from '../types/product';
import type { Product, StockStatus } from '../types/product';
import type { CartItem } from '../types/order';

export function getCartTotal(items: CartItem[]): number {
  return items.reduce((sum, item) => sum + item.product.price * item.quantity, 0);
}

export function getCartItemCount(items: CartItem[]): number {
  return items.reduce((sum, item) => sum + item.quantity, 0);
}

export function filterProducts(
  products: Product[],
  filters: {
    search?: string;
    category?: string;
    texture?: string;
    length?: string;
    hairType?: string;
    minPrice?: number;
    maxPrice?: number;
    availability?: StockStatus;
  }
): Product[] {
  return products.filter((product) => {
    if (product.status !== 'active') return false;
    if (filters.search) {
      const q = filters.search.toLowerCase();
      if (!product.name.toLowerCase().includes(q) && !product.description.toLowerCase().includes(q)) return false;
    }
    if (filters.category && product.category !== filters.category) return false;
    if (filters.texture && product.texture !== filters.texture) return false;
    if (filters.length && product.length !== filters.length) return false;
    if (filters.hairType && product.hairType !== filters.hairType) return false;
    if (filters.minPrice !== undefined && product.price < filters.minPrice) return false;
    if (filters.maxPrice !== undefined && product.price > filters.maxPrice) return false;
    if (filters.availability) {
      const status = getStockStatus(product);
      if (status !== filters.availability) return false;
    }
    return true;
  });
}

export function sortProducts(
  products: Product[],
  sortBy: 'name-asc' | 'name-desc' | 'price-asc' | 'price-desc' | 'newest'
): Product[] {
  const sorted = [...products];
  switch (sortBy) {
    case 'name-asc': return sorted.sort((a, b) => a.name.localeCompare(b.name));
    case 'name-desc': return sorted.sort((a, b) => b.name.localeCompare(a.name));
    case 'price-asc': return sorted.sort((a, b) => a.price - b.price);
    case 'price-desc': return sorted.sort((a, b) => b.price - a.price);
    case 'newest': return sorted.sort((a, b) => (b.isNew ? 1 : 0) - (a.isNew ? 1 : 0));
    default: return sorted;
  }
}

export function placeholderImg(seed: string, w = 600, h = 800): string {
  return `https://picsum.photos/seed/${seed}/${w}/${h}`;
}
