import { useEffect, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { Link } from 'react-router-dom';
import { Package, Layers, ShoppingCart, DollarSign, TrendingDown, TrendingUp, AlertTriangle, XCircle } from 'lucide-react';
import { AdminHeader } from '../../components/admin/AdminSidebar';
import { StatCard } from '../../components/admin/StatCard';
import { Badge, orderStatusBadgeVariant, orderStatusLabel } from '../../components/ui/Badge';
import { productService } from '../../services/productService';
import { orderService } from '../../services/orderService';
import { inventoryService } from '../../services/inventoryService';
import { purchaseService } from '../../services/purchaseService';
import { getStockStatus } from '../../types/product';
import type { Product } from '../../types/product';
import type { Order } from '../../types/order';
import { formatPrice, formatDate } from '../../utils/formatting';

interface OutletCtx { onMenuClick: () => void; }

export default function Dashboard() {
  const { onMenuClick } = useOutletContext<OutletCtx>();
  const [products, setProducts] = useState<Product[]>([]);
  const [orders, setOrders] = useState<Order[]>([]);
  const [stockIn, setStockIn] = useState(0);
  const [stockOut, setStockOut] = useState(0);
  const [monthlySpend, setMonthlySpend] = useState(0);

  useEffect(() => {
    productService.getAll().then(setProducts);
    orderService.getAll().then(setOrders);
    inventoryService.getStockSummary().then(({ totalIn, totalOut }) => {
      setStockIn(totalIn); setStockOut(totalOut);
    });
    purchaseService.getTotalThisMonth().then(setMonthlySpend);
  }, []);

  const activeProducts = products.filter((p) => p.status === 'active');
  const totalStock = products.reduce((s, p) => s + p.stockQuantity, 0);
  const lowStockProducts = products.filter((p) => getStockStatus(p) === 'low-stock');
  const outOfStock = products.filter((p) => getStockStatus(p) === 'out-of-stock');
  const deliveredOrders = orders.filter((o) => o.status === 'delivered');
  const salesRevenue = deliveredOrders.reduce((s, o) => s + o.total, 0);
  const recentOrders = orders.slice(0, 5);

  return (
    <>
      <AdminHeader title="Dashboard" onMenuClick={onMenuClick} />

      <main className="p-4 md:p-6 space-y-8">
        {/* Stats grid */}
        <section>
          <div className="grid grid-cols-2 xl:grid-cols-4 gap-3">
            <StatCard label="Total Products" value={activeProducts.length} sub="Active listings" icon={<Package size={18} />} />
            <StatCard label="Current Stock" value={totalStock} sub="Total units" icon={<Layers size={18} />} />
            <StatCard label="Items Sold" value={stockOut} sub="Units dispatched" icon={<ShoppingCart size={18} />} />
            <StatCard label="Sales Revenue" value={formatPrice(salesRevenue)} sub="From delivered orders" icon={<DollarSign size={18} />} accent />
          </div>
        </section>

        {/* Stock movement + Spend */}
        <section>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <div className="bg-white border border-rs-border p-5 flex gap-4 items-center">
              <div className="w-10 h-10 bg-green-50 flex items-center justify-center shrink-0">
                <TrendingUp size={18} className="text-green-600" />
              </div>
              <div>
                <p className="text-xs text-rs-muted uppercase tracking-wide">Stock In</p>
                <p className="text-2xl font-display font-bold text-rs-ink">{stockIn}</p>
                <p className="text-xs text-rs-muted">units received</p>
              </div>
            </div>
            <div className="bg-white border border-rs-border p-5 flex gap-4 items-center">
              <div className="w-10 h-10 bg-red-50 flex items-center justify-center shrink-0">
                <TrendingDown size={18} className="text-red-500" />
              </div>
              <div>
                <p className="text-xs text-rs-muted uppercase tracking-wide">Stock Out</p>
                <p className="text-2xl font-display font-bold text-rs-ink">{stockOut}</p>
                <p className="text-xs text-rs-muted">units dispatched</p>
              </div>
            </div>
            <div className="bg-white border border-rs-border p-5 flex gap-4 items-center">
              <div className="w-10 h-10 bg-rs-surface flex items-center justify-center shrink-0">
                <DollarSign size={18} className="text-rs-accent" />
              </div>
              <div>
                <p className="text-xs text-rs-muted uppercase tracking-wide">Purchase Spend (Aug)</p>
                <p className="text-xl font-display font-bold text-rs-ink">{formatPrice(monthlySpend)}</p>
                <p className="text-xs text-rs-muted">this month</p>
              </div>
            </div>
          </div>
        </section>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Recent orders */}
          <section className="lg:col-span-2 bg-white border border-rs-border">
            <div className="flex items-center justify-between px-5 py-4 border-b border-rs-border">
              <h2 className="font-display font-semibold text-rs-ink">Recent Orders</h2>
              <Link to="/admin/orders" className="text-xs text-rs-muted hover:text-rs-ink transition-colors">View all →</Link>
            </div>
            <div className="overflow-x-auto">
              <table className="admin-table">
                <thead><tr><th>Order #</th><th>Customer</th><th>Total</th><th>Date</th><th>Status</th></tr></thead>
                <tbody>
                  {recentOrders.map((o) => (
                    <tr key={o.id}>
                      <td><Link to={`/admin/orders/${o.id}`} className="font-mono text-xs text-rs-ink hover:text-rs-accent">{o.orderNumber}</Link></td>
                      <td className="text-xs">{o.customer.fullName}</td>
                      <td className="font-medium text-xs">{formatPrice(o.total)}</td>
                      <td className="text-xs text-rs-muted">{formatDate(o.createdAt)}</td>
                      <td><Badge variant={orderStatusBadgeVariant(o.status)}>{orderStatusLabel(o.status)}</Badge></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>

          {/* Stock alerts */}
          <section className="bg-white border border-rs-border">
            <div className="flex items-center justify-between px-5 py-4 border-b border-rs-border">
              <h2 className="font-display font-semibold text-rs-ink">Stock Alerts</h2>
              <Link to="/admin/inventory" className="text-xs text-rs-muted hover:text-rs-ink transition-colors">View all →</Link>
            </div>
            <div className="p-4 space-y-3">
              {outOfStock.map((p) => (
                <div key={p.id} className="flex items-center gap-3 p-3 bg-red-50 border border-red-100">
                  <XCircle size={15} className="text-red-500 shrink-0" />
                  <div className="min-w-0">
                    <p className="text-xs font-semibold text-rs-ink truncate">{p.name}</p>
                    <p className="text-[11px] text-red-500">Out of stock</p>
                  </div>
                </div>
              ))}
              {lowStockProducts.map((p) => (
                <div key={p.id} className="flex items-center gap-3 p-3 bg-amber-50 border border-amber-100">
                  <AlertTriangle size={15} className="text-amber-600 shrink-0" />
                  <div className="min-w-0">
                    <p className="text-xs font-semibold text-rs-ink truncate">{p.name}</p>
                    <p className="text-[11px] text-amber-600">Only {p.stockQuantity} left</p>
                  </div>
                </div>
              ))}
              {outOfStock.length === 0 && lowStockProducts.length === 0 && (
                <p className="text-xs text-rs-muted text-center py-4">All stock levels are healthy.</p>
              )}
            </div>
          </section>
        </div>
      </main>
    </>
  );
}
