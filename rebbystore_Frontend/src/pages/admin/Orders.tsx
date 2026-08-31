import { useEffect, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { AdminHeader } from '../../components/admin/AdminSidebar';
import { OrderTable } from '../../components/admin/OrderTable';
import { orderService } from '../../services/orderService';
import type { Order, OrderStatus } from '../../types/order';
import { formatPrice } from '../../utils/formatting';

interface OutletCtx { onMenuClick: () => void; }

const STATUS_TABS: { value: 'all' | OrderStatus; label: string }[] = [
  { value: 'all', label: 'All' },
  { value: 'pending', label: 'Pending' },
  { value: 'confirmed', label: 'Confirmed' },
  { value: 'processing', label: 'Processing' },
  { value: 'ready-for-delivery', label: 'Ready' },
  { value: 'delivered', label: 'Delivered' },
  { value: 'cancelled', label: 'Cancelled' },
];

export default function Orders() {
  const { onMenuClick } = useOutletContext<OutletCtx>();
  const [orders, setOrders] = useState<Order[]>([]);
  const [activeTab, setActiveTab] = useState<'all' | OrderStatus>('all');

  useEffect(() => { orderService.getAll().then(setOrders); }, []);

  const filtered = activeTab === 'all' ? orders : orders.filter((o) => o.status === activeTab);
  const totalRevenue = orders.filter((o) => o.status === 'delivered').reduce((s, o) => s + o.total, 0);

  function countByStatus(s: OrderStatus) { return orders.filter((o) => o.status === s).length; }

  return (
    <>
      <AdminHeader title="Orders" onMenuClick={onMenuClick} />
      <main className="p-4 md:p-6 space-y-5">
        {/* Stats */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          <div className="bg-white border border-rs-border p-4">
            <p className="section-label mb-1">Total Orders</p>
            <p className="text-2xl font-display font-bold text-rs-ink">{orders.length}</p>
          </div>
          <div className="bg-white border border-rs-border p-4">
            <p className="section-label mb-1">Pending</p>
            <p className="text-2xl font-display font-bold text-purple-600">{countByStatus('pending')}</p>
          </div>
          <div className="bg-white border border-rs-border p-4">
            <p className="section-label mb-1">Delivered</p>
            <p className="text-2xl font-display font-bold text-green-600">{countByStatus('delivered')}</p>
          </div>
          <div className="bg-rs-ink text-white border border-rs-ink p-4">
            <p className="text-xs tracking-wider uppercase text-white/60 mb-1">Revenue</p>
            <p className="text-xl font-display font-bold">{formatPrice(totalRevenue)}</p>
          </div>
        </div>

        {/* Status filter tabs */}
        <div className="flex gap-0 border-b border-rs-border overflow-x-auto">
          {STATUS_TABS.map((tab) => {
            const count = tab.value === 'all' ? orders.length : countByStatus(tab.value as OrderStatus);
            return (
              <button
                key={tab.value}
                onClick={() => setActiveTab(tab.value)}
                className={`whitespace-nowrap px-4 py-2.5 text-sm font-medium border-b-2 transition-colors -mb-px ${
                  activeTab === tab.value
                    ? 'border-rs-ink text-rs-ink'
                    : 'border-transparent text-rs-muted hover:text-rs-ink'
                }`}
              >
                {tab.label}
                <span className="ml-1.5 text-xs text-rs-muted">({count})</span>
              </button>
            );
          })}
        </div>

        {/* Table */}
        <div className="bg-white border border-rs-border overflow-x-auto">
          {filtered.length > 0
            ? <OrderTable orders={filtered} />
            : <p className="text-center py-10 text-rs-muted text-sm">No orders with this status.</p>
          }
        </div>
      </main>
    </>
  );
}
