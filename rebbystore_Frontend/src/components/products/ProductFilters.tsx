import { X, SlidersHorizontal } from 'lucide-react';
import { storeConfig } from '../../config/store';
import { Button } from '../ui/Button';

export interface FilterState {
  search: string;
  category: string;
  texture: string;
  length: string;
  hairType: string;
  minPrice: string;
  maxPrice: string;
  sortBy: string;
}

interface ProductFiltersProps {
  filters: FilterState;
  onChange: (key: keyof FilterState, value: string) => void;
  onReset: () => void;
  totalCount: number;
  mobileOpen: boolean;
  onMobileToggle: () => void;
}

export const DEFAULT_FILTERS: FilterState = {
  search: '',
  category: '',
  texture: '',
  length: '',
  hairType: '',
  minPrice: '',
  maxPrice: '',
  sortBy: 'newest',
};

const sortOptions = [
  { value: 'newest', label: 'Newest First' },
  { value: 'price-asc', label: 'Price: Low to High' },
  { value: 'price-desc', label: 'Price: High to Low' },
  { value: 'name-asc', label: 'Name: A–Z' },
  { value: 'name-desc', label: 'Name: Z–A' },
];

function FilterSection({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="py-4 border-b border-rs-border last:border-0">
      <p className="section-label mb-3">{title}</p>
      {children}
    </div>
  );
}

function FilterChip({
  label,
  active,
  onClick,
}: {
  label: string;
  active: boolean;
  onClick: () => void;
}) {
  return (
    <button
      onClick={onClick}
      className={`px-3 py-1.5 text-xs border transition-colors duration-200 ${
        active
          ? 'bg-rs-ink text-white border-rs-ink'
          : 'bg-white text-rs-muted border-rs-border hover:border-rs-ink hover:text-rs-ink'
      }`}
    >
      {label}
    </button>
  );
}

function CategoryOption({
  label,
  active,
  onClick,
}: {
  label: string;
  active: boolean;
  onClick: () => void;
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`group flex w-full items-center justify-between py-2.5 text-sm transition-colors duration-200 ${
        active
          ? 'text-rs-ink font-semibold'
          : 'text-rs-muted hover:text-rs-ink'
      }`}
    >
      <span>{label}</span>

      <span
        className={`h-1.5 w-1.5 rounded-full transition-all duration-200 ${
          active
            ? 'bg-rs-accent scale-100'
            : 'bg-transparent scale-0 group-hover:bg-rs-border group-hover:scale-100'
        }`}
        aria-hidden="true"
      />
    </button>
  );
}


export function ProductFilters({
  filters,
  onChange,
  onReset,
  totalCount,
  mobileOpen,
  onMobileToggle,
}: ProductFiltersProps) {
  const hasActiveFilters =
    filters.category || filters.texture || filters.length || filters.hairType || filters.minPrice || filters.maxPrice;

  const filterPanel = (
    <div>
      {/* Results count + reset */}
      <div className="flex items-center justify-between py-3 border-b border-rs-border">
        <p className="text-sm text-rs-muted">
          <span className="font-semibold text-rs-ink">{totalCount}</span> products
        </p>
        {hasActiveFilters && (
          <button onClick={onReset} className="text-xs text-rs-muted hover:text-rs-ink flex items-center gap-1 transition-colors">
            <X size={12} /> Clear filters
          </button>
        )}
      </div>

      {/* Category */}
<FilterSection title="Category">
  <div className="space-y-0.5">
    <CategoryOption
      label="All Products"
      active={!filters.category}
      onClick={() => onChange('category', '')}
    />

    {storeConfig.categories.map((cat) => (
      <CategoryOption
        key={cat.id}
        label={cat.label}
        active={filters.category === cat.id}
        onClick={() =>
          onChange(
            'category',
            filters.category === cat.id ? '' : cat.id
          )
        }
      />
    ))}
  </div>
</FilterSection>

      {/* Hair Type */}
      <FilterSection title="Hair Type">
        <div className="flex flex-wrap gap-2">
          {storeConfig.hairTypes.map((ht) => (
            <FilterChip
              key={ht.id}
              label={ht.label}
              active={filters.hairType === ht.id}
              onClick={() => onChange('hairType', filters.hairType === ht.id ? '' : ht.id)}
            />
          ))}
        </div>
      </FilterSection>

      {/* Texture */}
      <FilterSection title="Texture">
        <div className="flex flex-wrap gap-2">
          {storeConfig.textures.map((t) => (
            <FilterChip
              key={t.id}
              label={t.label}
              active={filters.texture === t.id}
              onClick={() => onChange('texture', filters.texture === t.id ? '' : t.id)}
            />
          ))}
        </div>
      </FilterSection>

      {/* Length */}
      <FilterSection title="Length">
        <div className="flex flex-wrap gap-2">
          {storeConfig.lengths.map((l) => (
            <FilterChip
              key={l}
              label={l}
              active={filters.length === l}
              onClick={() => onChange('length', filters.length === l ? '' : l)}
            />
          ))}
        </div>
      </FilterSection>

      {/* Price */}
      <FilterSection title="Price (TSh)">
        <div className="flex items-center gap-2">
          <input
            type="number"
            placeholder="Min"
            value={filters.minPrice}
            onChange={(e) => onChange('minPrice', e.target.value)}
            className="w-full border border-rs-border px-3 py-2 text-sm focus:outline-none focus:border-rs-ink"
          />
          <span className="text-rs-muted text-sm shrink-0">–</span>
          <input
            type="number"
            placeholder="Max"
            value={filters.maxPrice}
            onChange={(e) => onChange('maxPrice', e.target.value)}
            className="w-full border border-rs-border px-3 py-2 text-sm focus:outline-none focus:border-rs-ink"
          />
        </div>
      </FilterSection>

      {/* Sort */}
      <FilterSection title="Sort By">
        <select
          value={filters.sortBy}
          onChange={(e) => onChange('sortBy', e.target.value)}
          className="w-full border border-rs-border px-3 py-2 text-sm text-rs-ink focus:outline-none focus:border-rs-ink bg-white"
        >
          {sortOptions.map((opt) => (
            <option key={opt.value} value={opt.value}>{opt.label}</option>
          ))}
        </select>
      </FilterSection>
    </div>
  );

  return (
    <>
      {/* Mobile filter toggle */}
      <div className="lg:hidden mb-4">
        <Button
          variant="secondary"
          size="sm"
          onClick={onMobileToggle}
          className="flex items-center gap-2"
        >
          <SlidersHorizontal size={14} />
          Filters {hasActiveFilters && '•'}
        </Button>
      </div>

      {/* Mobile filter drawer */}
      {mobileOpen && (
        <div className="fixed inset-0 z-40 lg:hidden flex">
          <div className="absolute inset-0 bg-rs-ink/40" onClick={onMobileToggle} />
          <div className="relative bg-white w-72 ml-auto h-full overflow-y-auto p-5">
            <div className="flex items-center justify-between mb-2">
              <h2 className="font-display font-semibold">Filters</h2>
              <button onClick={onMobileToggle} aria-label="Close filters">
                <X size={18} className="text-rs-muted" />
              </button>
            </div>
            {filterPanel}
          </div>
        </div>
      )}

      {/* Desktop sidebar */}
      <div className="hidden lg:block w-56 shrink-0">{filterPanel}</div>
    </>
  );
}
