import type { Product } from '../../types/product';
import { ProductCard } from './ProductCard';
import { EmptyState } from '../ui/EmptyState';
import { ShoppingBag } from 'lucide-react';

interface ProductGridProps {
  products: Product[];
  loading?: boolean;
  emptyMessage?: string;
}

export function ProductGrid({ products, loading, emptyMessage }: ProductGridProps) {
  if (loading) {
    return (
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-3 md:gap-4">
        {Array.from({ length: 8 }).map((_, i) => (
          <div key={i} className="bg-rs-surface animate-pulse aspect-[3/4]" />
        ))}
      </div>
    );
  }

  if (products.length === 0) {
    return (
      <EmptyState
        icon={<ShoppingBag size={40} />}
        title="No products found"
        description={emptyMessage ?? 'Try adjusting your filters or search terms.'}
      />
    );
  }

  return (
    <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-3 md:gap-4">
      {products.map((product) => (
        <ProductCard key={product.id} product={product} />
      ))}
    </div>
  );
}
