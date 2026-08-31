export interface Product {
  id: string;
  name: string;
  sku: string;
  price: number;
  images: string[];
  description: string;
  category: 'human-hair' | 'synthetic' | 'lace-front' | 'closure' | 'bob';
  color: string;
  texture: 'straight' | 'wavy' | 'curly' | 'kinky' | 'yaki' | 'water-wave' | 'body-wave' | 'deep-wave' | 'loose-wave';
  length: string;
  hairType: 'human' | 'synthetic' | 'blend';
  stockQuantity: number;
  lowStockThreshold: number;
  status: 'active' | 'inactive';
  isNew?: boolean;
  isFeatured?: boolean;
  originalPrice?: number;
}

export type ProductCategory = Product['category'];
export type ProductTexture = Product['texture'];
export type ProductHairType = Product['hairType'];
export type StockStatus = 'in-stock' | 'low-stock' | 'out-of-stock';

export function getStockStatus(product: Product): StockStatus {
  if (product.stockQuantity === 0) return 'out-of-stock';
  if (product.stockQuantity <= product.lowStockThreshold) return 'low-stock';
  return 'in-stock';
}
