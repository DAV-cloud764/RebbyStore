import type { InventoryMovement } from '../../types/inventory';
import { formatDate } from '../../utils/formatting';
import { ArrowDown, ArrowUp } from 'lucide-react';

interface StockMovementTableProps { movements: InventoryMovement[]; }

const reasonLabel: Record<string, string> = {
  purchase: 'Purchase', sale: 'Sale', damaged: 'Damaged',
  lost: 'Lost', 'returned-to-supplier': 'Returned to Supplier', adjustment: 'Adjustment',
};

export function StockMovementTable({ movements }: StockMovementTableProps) {
  return (
    <div className="overflow-x-auto">
      <table className="admin-table">
        <thead>
          <tr>
            <th>Date</th>
            <th>Product</th>
            <th>SKU</th>
            <th>Type</th>
            <th>Qty</th>
            <th>Reason</th>
            <th>Reference</th>
          </tr>
        </thead>
        <tbody>
          {movements.map((m) => (
            <tr key={m.id}>
              <td className="text-xs text-rs-muted">{formatDate(m.date)}</td>
              <td className="font-medium text-xs">{m.productName}</td>
              <td className="font-mono text-xs text-rs-muted">{m.sku}</td>
              <td>
                <span className={`inline-flex items-center gap-1 text-xs font-semibold ${m.type === 'in' ? 'text-green-600' : 'text-red-500'}`}>
                  {m.type === 'in' ? <ArrowUp size={12} /> : <ArrowDown size={12} />}
                  {m.type === 'in' ? 'Stock In' : 'Stock Out'}
                </span>
              </td>
              <td>
                <span className={`font-bold text-sm ${m.type === 'in' ? 'text-green-600' : 'text-red-500'}`}>
                  {m.type === 'in' ? '+' : '–'}{m.quantity}
                </span>
              </td>
              <td className="text-xs text-rs-muted">{reasonLabel[m.reason] ?? m.reason}</td>
              <td className="font-mono text-xs text-rs-muted">{m.reference ?? '—'}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
