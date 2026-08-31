import { Link } from 'react-router-dom';
import { Eye } from 'lucide-react';
import type { Order } from '../../types/order';
import { Badge, orderStatusBadgeVariant, orderStatusLabel } from '../ui/Badge';
import { formatPrice, formatDate } from '../../utils/formatting';

interface OrderTableProps { orders: Order[]; }

export function OrderTable({ orders }: OrderTableProps) {
  return (
    <div className="overflow-x-auto">
      <table className="admin-table">
        <thead>
          <tr>
            <th>Order #</th>
            <th>Customer</th>
            <th>Items</th>
            <th>Total</th>
            <th>Date</th>
            <th>Status</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {orders.map((order) => (
            <tr key={order.id}>
              <td className="font-mono text-xs">{order.orderNumber}</td>
              <td>
                <p className="font-medium text-rs-ink text-xs">{order.customer.fullName}</p>
                <p className="text-rs-muted text-[11px]">{order.customer.phone}</p>
              </td>
              <td className="text-rs-muted">{order.items.length} item{order.items.length !== 1 ? 's' : ''}</td>
              <td className="font-medium">{formatPrice(order.total)}</td>
              <td className="text-rs-muted text-xs">{formatDate(order.createdAt)}</td>
              <td>
                <Badge variant={orderStatusBadgeVariant(order.status)}>
                  {orderStatusLabel(order.status)}
                </Badge>
              </td>
              <td>
                <Link to={`/admin/orders/${order.id}`} className="text-rs-muted hover:text-rs-ink transition-colors p-1 inline-block" aria-label="View order">
                  <Eye size={15} />
                </Link>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
