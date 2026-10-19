import { useEffect, useState } from 'react';
import { useOutletContext, Link } from 'react-router-dom';
import { Plus, Edit2, Eye, Search, Trash2 } from 'lucide-react';
import { AdminHeader } from '../../components/admin/AdminSidebar';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { productService } from '../../services/productService';
import { getStockStatus } from '../../types/product';
import type { Product } from '../../types/product';
import { formatPrice } from '../../utils/formatting';
import { useToast } from '../../contexts/ToastContext';

interface OutletCtx { onMenuClick: () => void; }

export default function Products() {
  const { onMenuClick } = useOutletContext<OutletCtx>();
  const [products, setProducts] = useState<Product[]>([]);
  const [search, setSearch] = useState('');

  const [deletingId, setDeletingId] = useState<string | null>(null);
  const { showToast } = useToast();

  useEffect(() => { productService.getAll().then(setProducts); }, []);

  async function handleRemove(product: Product) {
  const confirmed = window.confirm(
    `Remove "${product.name}" from the catalog?\n\n` +
    'The product will be deactivated, not permanently deleted. ' +
    'It will no longer appear in the customer-facing catalog.'
  );

  if (!confirmed) return;

  setDeletingId(product.id);

  try {
    await productService.delete(product.id);

    setProducts((current) =>
      current.filter((item) => item.id !== product.id)
    );

    showToast(
      `${product.name} was removed from the catalog.`,
      'success'
    );
  } catch (error) {
    showToast(
      error instanceof Error
        ? error.message
        : 'Failed to remove the product. Please try again.',
      'error'
    );
  } finally {
    setDeletingId(null);
  }
}

  const filtered = products.filter(
    (p) => p.name.toLowerCase().includes(search.toLowerCase()) || p.sku.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <>
      <AdminHeader
        title="Products"
        onMenuClick={onMenuClick}
        actions={
          <Link to="/admin/products/new">
            <Button size="sm"><Plus size={14} /> Add Product</Button>
          </Link>
        }
      />

      <main className="p-4 md:p-6">
        {/* Search */}
        <div className="mb-5 relative max-w-xs">
          <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-rs-muted" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search products or SKU…"
            className="w-full pl-9 border border-rs-border bg-white px-4 py-2.5 text-sm focus:outline-none focus:border-rs-ink"
          />
        </div>

        <div className="bg-white border border-rs-border overflow-x-auto">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Product</th>
                <th>SKU</th>
                <th>Category</th>
                <th>Price</th>
                <th>Stock</th>
                <th>Stock Status</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((p) => {
                const ss = getStockStatus(p);
                return (
                  <tr key={p.id}>
                    <td>
                      <div className="flex items-center gap-3">
                        <img src={p.images[0]} alt="" className="w-8 h-10 object-cover bg-rs-surface shrink-0" />
                        <div>
                          <p className="font-medium text-xs text-rs-ink">{p.name}</p>
                          <p className="text-[11px] text-rs-muted">{p.length} · {p.color}</p>
                        </div>
                      </div>
                    </td>
                    <td className="font-mono text-xs text-rs-muted">{p.sku}</td>
                    <td className="text-xs text-rs-muted capitalize">{p.category.replace('-', ' ')}</td>
                    <td className="font-medium text-xs">{formatPrice(p.price)}</td>
                    <td>
                      <span className={`font-semibold text-sm ${
                        ss === 'out-of-stock' ? 'text-red-600' :
                        ss === 'low-stock' ? 'text-amber-600' : 'text-rs-ink'
                      }`}>
                        {p.stockQuantity}
                      </span>
                      <span className="text-[11px] text-rs-muted"> / threshold {p.lowStockThreshold}</span>
                    </td>
                    <td>
                      <Badge variant={ss === 'in-stock' ? 'in-stock' : ss === 'low-stock' ? 'low-stock' : 'out-of-stock'}>
                        {ss === 'in-stock' ? 'In Stock' : ss === 'low-stock' ? 'Low Stock' : 'Out of Stock'}
                      </Badge>
                    </td>
                    <td>
                      <Badge variant={p.status === 'active' ? 'in-stock' : 'neutral'}>
                        {p.status}
                      </Badge>
                    </td>
                    <td>
                      <div className="flex items-center gap-1">
                        <Link to={`/product/${p.id}`} className="p-1.5 text-rs-muted hover:text-rs-ink transition-colors" title="View on store">
                          <Eye size={14} />
                        </Link>
                        <Link to={`/admin/products/${p.id}/edit`} className="p-1.5 text-rs-muted hover:text-rs-ink transition-colors" title="Edit product">
                          <Edit2 size={14} />
                        </Link>

<button
  type="button"
  onClick={() => void handleRemove(p)}
  disabled={deletingId !== null}
  className="p-1.5 text-red-600 hover:text-red-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
  title="Remove from catalog"
  aria-label={`Remove ${p.name} from catalog`}
>
  {deletingId === p.id ? (
    <span className="text-xs">Removing…</span>
  ) : (
    <Trash2 size={14} />
  )}
</button>
                      </div>
                    </td>
                  </tr>
                );
              })}
              {filtered.length === 0 && (
                <tr><td colSpan={8} className="text-center py-10 text-rs-muted text-sm">No products found.</td></tr>
              )}
            </tbody>
          </table>
        </div>
        <p className="text-xs text-rs-muted mt-3">{filtered.length} product{filtered.length !== 1 ? 's' : ''} shown</p>
      </main>
    </>
  );
}
