import { useEffect, useState } from 'react';
import { useOutletContext, Link } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import { AdminHeader } from '../../components/admin/AdminSidebar';
import { StockMovementTable } from '../../components/admin/StockMovementTable';
import { inventoryService } from '../../services/inventoryService';
import type { InventoryMovement } from '../../types/inventory';

interface OutletCtx { onMenuClick: () => void; }

export default function InventoryMovements() {
  const { onMenuClick } = useOutletContext<OutletCtx>();
  const [movements, setMovements] = useState<InventoryMovement[]>([]);
  const [filter, setFilter] = useState<'all' | 'in' | 'out'>('all');

  useEffect(() => { inventoryService.getMovements().then(setMovements); }, []);

  const filtered = filter === 'all' ? movements : movements.filter((m) => m.type === filter);

  const totalIn = movements.filter((m) => m.type === 'in').reduce((s, m) => s + m.quantity, 0);
  const totalOut = movements.filter((m) => m.type === 'out').reduce((s, m) => s + m.quantity, 0);

  return (
    <>
      <AdminHeader
        title="Stock Movements"
        onMenuClick={onMenuClick}
        actions={
          <Link to="/admin/inventory" className="text-sm text-rs-muted hover:text-rs-ink flex items-center gap-1 transition-colors">
            <ArrowLeft size={14} /> Stock Overview
          </Link>
        }
      />
      <main className="p-4 md:p-6 space-y-5">
        {/* Summary cards */}
        <div className="grid grid-cols-3 gap-3">
          <div className="bg-white border border-rs-border p-4">
            <p className="section-label mb-1">Total Movements</p>
            <p className="text-2xl font-display font-bold text-rs-ink">{movements.length}</p>
          </div>
          <div className="bg-green-50 border border-green-200 p-4">
            <p className="section-label text-green-600 mb-1">Total In</p>
            <p className="text-2xl font-display font-bold text-green-700">+{totalIn}</p>
          </div>
          <div className="bg-red-50 border border-red-200 p-4">
            <p className="section-label text-red-500 mb-1">Total Out</p>
            <p className="text-2xl font-display font-bold text-red-600">–{totalOut}</p>
          </div>
        </div>

        {/* Filter tabs */}
        <div className="flex gap-1 border-b border-rs-border">
          {(['all', 'in', 'out'] as const).map((t) => (
            <button
              key={t}
              onClick={() => setFilter(t)}
              className={`px-5 py-2.5 text-sm font-medium capitalize border-b-2 transition-colors -mb-px ${
                filter === t
                  ? 'border-rs-ink text-rs-ink'
                  : 'border-transparent text-rs-muted hover:text-rs-ink'
              }`}
            >
              {t === 'all' ? 'All' : t === 'in' ? 'Stock In' : 'Stock Out'}
              <span className="ml-2 text-xs text-rs-muted">
                ({t === 'all' ? movements.length : movements.filter((m) => m.type === t).length})
              </span>
            </button>
          ))}
        </div>

        {/* Table */}
        <div className="bg-white border border-rs-border overflow-x-auto">
          {filtered.length > 0
            ? <StockMovementTable movements={filtered} />
            : <p className="text-center py-10 text-rs-muted text-sm">No movements found.</p>
          }
        </div>
      </main>
    </>
  );
}
