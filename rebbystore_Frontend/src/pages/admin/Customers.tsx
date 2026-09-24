import { useEffect, useMemo, useState } from 'react';
import { Search } from 'lucide-react';
import { useOutletContext } from 'react-router-dom';
import { AdminHeader } from '../../components/admin/AdminSidebar';
import { customerService } from '../../services/customerService';
import type { Customer } from '../../types/customer';
import { formatPrice, formatDate } from '../../utils/formatting';

interface OutletCtx {
  onMenuClick: () => void;
}

export default function Customers() {
  const { onMenuClick } = useOutletContext<OutletCtx>();

  const [customers, setCustomers] = useState<Customer[]>([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let mounted = true;

    async function loadCustomers() {
      try {
        setLoading(true);
        setError('');

        const data = await customerService.getAll();

        if (mounted) {
          setCustomers(data);
        }
      } catch (err) {
        if (mounted) {
          setError(
            err instanceof Error
              ? err.message
              : 'Failed to load customers'
          );
        }
      } finally {
        if (mounted) {
          setLoading(false);
        }
      }
    }

    loadCustomers();

    return () => {
      mounted = false;
    };
  }, []);

  const filtered = useMemo(() => {
    const query = search.trim().toLowerCase();

    if (!query) {
      return customers;
    }

    return customers.filter((customer) =>
      customer.fullName.toLowerCase().includes(query) ||
      customer.phone.includes(query) ||
      customer.email.toLowerCase().includes(query)
    );
  }, [customers, search]);

  const totalOrders = useMemo(
    () =>
      customers.reduce(
        (sum, customer) => sum + customer.totalOrders,
        0
      ),
    [customers]
  );

  const totalRevenue = useMemo(
    () =>
      customers.reduce(
        (sum, customer) => sum + customer.totalSpent,
        0
      ),
    [customers]
  );

  return (
    <>
      <AdminHeader
        title="Customers"
        onMenuClick={onMenuClick}
      />

      <main className="p-4 md:p-6 space-y-5">

        {/* Stats */}
        <div className="grid grid-cols-3 gap-3">
          <div className="bg-white border border-rs-border p-4">
            <p className="section-label mb-1">
              Total Customers
            </p>
            <p className="text-2xl font-display font-bold text-rs-ink">
              {loading ? '—' : customers.length}
            </p>
          </div>

          <div className="bg-white border border-rs-border p-4">
            <p className="section-label mb-1">
              Total Orders
            </p>
            <p className="text-2xl font-display font-bold text-rs-ink">
              {loading ? '—' : totalOrders}
            </p>
          </div>

          <div className="bg-rs-ink text-white p-4 border border-rs-ink">
            <p className="text-xs uppercase tracking-wider text-white/60 mb-1">
              Lifetime Value
            </p>
            <p className="text-xl font-display font-bold">
              {loading ? '—' : formatPrice(totalRevenue)}
            </p>
          </div>
        </div>

        {/* Search */}
        <div className="relative max-w-xs">
          <Search
            size={14}
            className="absolute left-3 top-1/2 -translate-y-1/2 text-rs-muted"
          />

          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search name, phone, email…"
            className="w-full pl-9 border border-rs-border bg-white px-4 py-2.5 text-sm focus:outline-none focus:border-rs-ink"
          />
        </div>

        {/* Error */}
        {error && (
          <div className="border border-red-200 bg-red-50 text-red-700 px-4 py-3 text-sm">
            {error}
          </div>
        )}

        {/* Table */}
        <div className="bg-white border border-rs-border overflow-x-auto">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Customer</th>
                <th>Contact</th>
                <th>Orders</th>
                <th>Total Spent</th>
                <th>Created</th>
              </tr>
            </thead>

            <tbody>
              {loading ? (
                <tr>
                  <td
                    colSpan={5}
                    className="text-center py-10 text-rs-muted text-sm"
                  >
                    Loading customers…
                  </td>
                </tr>
              ) : (
                <>
                  {filtered.map((customer) => (
                    <tr key={customer.id}>
                      <td className="font-semibold text-xs text-rs-ink">
                        {customer.fullName}
                      </td>

                      <td>
                        <p className="text-xs">
                          {customer.phone}
                        </p>
                        <p className="text-[11px] text-rs-muted">
                          {customer.email}
                        </p>
                      </td>

                      <td>
                        <span className="font-semibold text-rs-ink">
                          {customer.totalOrders}
                        </span>
                      </td>

                      <td className="font-semibold text-xs">
                        {formatPrice(customer.totalSpent)}
                      </td>

                      <td className="text-xs text-rs-muted">
                        {formatDate(customer.createdAt)}
                      </td>
                    </tr>
                  ))}

                  {filtered.length === 0 && (
                    <tr>
                      <td
                        colSpan={5}
                        className="text-center py-10 text-rs-muted text-sm"
                      >
                        No customers found.
                      </td>
                    </tr>
                  )}
                </>
              )}
            </tbody>
          </table>
        </div>

        {!loading && (
          <p className="text-xs text-rs-muted">
            {filtered.length} customer
            {filtered.length !== 1 ? 's' : ''}
          </p>
        )}
      </main>
    </>
  );
}