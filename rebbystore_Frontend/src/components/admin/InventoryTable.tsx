import type { Product } from '../../types/product';
import { Badge } from '../ui/Badge';
import { formatPrice } from '../../utils/formatting';
import { getStockStatus } from '../../types/product';

interface InventoryTableProps { products: Product[]; }

export function InventoryTable({ products }: InventoryTableProps) {
  return (
    <div className="overflow-x-auto">
      <table className="admin-table">
        <thead>
          <tr>
            <th>Product</th>
            <th>SKU</th>
            <th>Category</th>
            <th>Price</th>
            <th>Stock</th>
            <th>Threshold</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          {products.map((p) => {
            const ss = getStockStatus(p);
            return (
              <tr key={p.id}>
                <td>
                  <div className="flex items-center gap-3">
                    <img src={p.images[0]} alt="" className="w-8 h-10 object-cover bg-rs-surface shrink-0" />
                    <span className="font-medium text-xs">{p.name}</span>
                  </div>
                </td>
                <td className="font-mono text-xs text-rs-muted">{p.sku}</td>
                <td className="text-rs-muted text-xs capitalize">{p.category.replace('-', ' ')}</td>
                <td className="font-medium">{formatPrice(p.price)}</td>
                <td>
                  <span className={`font-semibold ${
                    ss === 'out-of-stock' ? 'text-red-600' :
                    ss === 'low-stock' ? 'text-amber-600' : 'text-rs-ink'
                  }`}>
                    {p.stockQuantity}
                  </span>
                </td>
                <td className="text-rs-muted">{p.lowStockThreshold}</td>
                <td>
                  <Badge variant={ss === 'in-stock' ? 'in-stock' : ss === 'low-stock' ? 'low-stock' : 'out-of-stock'}>
                    {ss === 'in-stock' ? 'In Stock' : ss === 'low-stock' ? 'Low Stock' : 'Out of Stock'}
                  </Badge>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
