import { useEffect, useState, useMemo } from 'react';
import { useSearchParams } from 'react-router-dom';
import { ProductGrid } from '../../components/products/ProductGrid';
import { ProductFilters, DEFAULT_FILTERS } from '../../components/products/ProductFilters';
import type { FilterState } from '../../components/products/ProductFilters';
import { productService } from '../../services/productService';
import type { Product } from '../../types/product';

import { filterProducts, sortProducts } from '../../utils/helpers';

export default function Shop() {
  const [searchParams] = useSearchParams();
  const [allProducts, setAllProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [mobileFiltersOpen, setMobileFiltersOpen] = useState(false);

  // Initialise filters from URL params
  const [filters, setFilters] = useState<FilterState>(() => ({
    ...DEFAULT_FILTERS,
    search: searchParams.get('search') ?? '',
    category: searchParams.get('category') ?? '',
    texture: searchParams.get('texture') ?? '',
  }));

  useEffect(() => {
    productService.getAll().then((products) => {
      setAllProducts(products);
      setLoading(false);
    });
  }, []);

  function handleChange(key: keyof FilterState, value: string) {
    setFilters((prev) => ({ ...prev, [key]: value }));
  }

  function handleReset() {
    setFilters(DEFAULT_FILTERS);
  }

  const filteredAndSorted = useMemo(() => {
    const filtered = filterProducts(allProducts, {
      search: filters.search,
      category: filters.category,
      texture: filters.texture,
      length: filters.length,
      hairType: filters.hairType,
      minPrice: filters.minPrice ? Number(filters.minPrice) : undefined,
      maxPrice: filters.maxPrice ? Number(filters.maxPrice) : undefined,
    });
    return sortProducts(filtered, filters.sortBy as Parameters<typeof sortProducts>[1]);
  }, [allProducts, filters]);

  return (
    <div className="page-container py-8">
      {/* Page header */}
      <div className="mb-8">
        <span className="section-label block mb-1">All Products</span>
        <h1 className="font-display text-4xl font-bold text-rs-ink">Shop</h1>
      </div>

      {/* Search bar */}
      <div className="mb-6">
        <div className="relative max-w-md">
          <input
            type="search"
            value={filters.search}
            onChange={(e) => handleChange('search', e.target.value)}
            placeholder="Search by name or description…"
            className="w-full border border-rs-border bg-white px-4 py-3 pr-10 text-sm focus:outline-none focus:border-rs-ink transition-colors"
            aria-label="Search products"
          />
          {filters.search && (
            <button
              onClick={() => handleChange('search', '')}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-rs-muted hover:text-rs-ink"
              aria-label="Clear search"
            >
              ×
            </button>
          )}
        </div>
      </div>

      <div className="flex gap-8">
        {/* Filters sidebar */}
        <ProductFilters
          filters={filters}
          onChange={handleChange}
          onReset={handleReset}
          totalCount={filteredAndSorted.length}
          mobileOpen={mobileFiltersOpen}
          onMobileToggle={() => setMobileFiltersOpen(!mobileFiltersOpen)}
        />

        {/* Product grid */}
        <div className="flex-1 min-w-0">
          <ProductGrid
            products={filteredAndSorted}
            loading={loading}
            emptyMessage="No products match your filters. Try clearing some to see more results."
          />
        </div>
      </div>
    </div>
  );
}
