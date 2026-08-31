import { useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { Search } from 'lucide-react';
import { AdminHeader } from '../../components/admin/AdminSidebar';
import { mockCustomers } from '../../data/mock/customers';
import type { Customer } from '../../types/customer';
import { formatPrice, formatDate } from '../../utils/formatting';

interface OutletCtx { onMenuClick: () => void; }

export default function Customers() {
  const { onMenuClick } = useOutletContext<OutletCtx>();
  const [customers] = useState<Customer[]>(mockCustomers);
  const [search, setSearch] = useState('');

  const filtered = customers.filter((c) =>
    c.fullName.toLowerCase().includes(search.toLowerCase()) ||
    c.phone.includes(search) ||
    c.email.toLowerCase().includes(search.toLowerCase())
  );

  const totalRevenue = customers.reduce((s, c) => s + c.totalSpent, 0);

  return (
    <>
      <AdminHeader title="Customers" onMenuClick={onMenuClick} />
      <main className="p-4 md:p-6 space-y-5">
        {/* Stats */}
        <div className="grid grid-cols-3 gap-3">
          <div className="bg-white border border-rs-border p-4">
            <p className="section-label mb-1">Total Customers</p>
            <p className="text-2xl font-display font-bold text-rs-ink">{customers.length}</p>
          </div>
          <div className="bg-white border border-rs-border p-4">
            <p className="section-label mb-1">Total Orders</p>
            <p className="text-2xl font-display font-bold text-rs-ink">{customers.reduce((s, c) => s + c.totalOrders, 0)}</p>
          </div>
          <div className="bg-rs-ink text-white p-4 border border-rs-ink">
            <p className="text-xs uppercase tracking-wider text-white/60 mb-1">Lifetime Value</p>
            <p className="text-xl font-display font-bold">{formatPrice(totalRevenue)}</p>
          </div>
        </div>

        {/* Search */}
        <div className="relative max-w-xs">
          <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-rs-muted" />
          <input type="text" value={search} onChange={(e) => setSearch(e.target.value)}
            placeholder="Search name, phone, email…"
            className="w-full pl-9 border border-rs-border bg-white px-4 py-2.5 text-sm focus:outline-none focus:border-rs-ink" />
        </div>

        {/* Table */}
        <div className="bg-white border border-rs-border overflow-x-auto">
          <table className="admin-table">
            <thead><tr><th>Customer</th><th>Contact</th><th>Location</th><th>Orders</th><th>Total Spent</th><th>Last Order</th></tr></thead>
            <tbody>
              {filtered.map((c) => (
                <tr key={c.id}>
                  <td className="font-semibold text-xs text-rs-ink">{c.fullName}</td>
                  <td>
                    <p className="text-xs">{c.phone}</p>
                    <p className="text-[11px] text-rs-muted">{c.email}</p>
                  </td>
                  <td className="text-xs text-rs-muted">{c.city && c.region ? `${c.city}, ${c.region}` : '—'}</td>
                  <td>
                    <span className="font-semibold text-rs-ink">{c.totalOrders}</span>
                  </td>
                  <td className="font-semibold text-xs">{formatPrice(c.totalSpent)}</td>
                  <td className="text-xs text-rs-muted">{c.lastOrderDate ? formatDate(c.lastOrderDate) : '—'}</td>
                </tr>
              ))}
              {filtered.length === 0 && (
                <tr><td colSpan={6} className="text-center py-10 text-rs-muted text-sm">No customers found.</td></tr>
              )}
            </tbody>
          </table>
        </div>
        <p className="text-xs text-rs-muted">{filtered.length} customer{filtered.length !== 1 ? 's' : ''}</p>
      </main>
    </>
  );
}
