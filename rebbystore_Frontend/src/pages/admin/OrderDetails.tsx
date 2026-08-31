import { useEffect, useState } from 'react';
import { useOutletContext, useParams, Link } from 'react-router-dom';
import { ArrowLeft, MapPin, Phone, Mail, Truck } from 'lucide-react';
import { AdminHeader } from '../../components/admin/AdminSidebar';
import { Badge, orderStatusBadgeVariant, orderStatusLabel } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Select } from '../../components/ui/Select';
import { orderService } from '../../services/orderService';
import type { Order, OrderStatus } from '../../types/order';
import { useToast } from '../../contexts/ToastContext';
import { formatPrice, formatDate, formatDateTime } from '../../utils/formatting';

interface OutletCtx { onMenuClick: () => void; }

const statusOptions: { value: OrderStatus; label: string }[] = [
  { value: 'pending', label: 'Pending' },
  { value: 'confirmed', label: 'Confirmed' },
  { value: 'processing', label: 'Processing' },
  { value: 'ready-for-delivery', label: 'Ready for Delivery' },
  { value: 'delivered', label: 'Delivered' },
  { value: 'cancelled', label: 'Cancelled' },
];

export default function OrderDetails() {
  const { onMenuClick } = useOutletContext<OutletCtx>();
  const { id } = useParams<{ id: string }>();
  const { showToast } = useToast();
  const [order, setOrder] = useState<Order | null>(null);
  const [newStatus, setNewStatus] = useState<OrderStatus>('pending');
  const [updating, setUpdating] = useState(false);

  useEffect(() => {
    if (!id) return;
    orderService.getById(id).then((o) => {
      if (o) { setOrder(o); setNewStatus(o.status); }
    });
  }, [id]);

  async function handleStatusUpdate() {
    if (!order || newStatus === order.status) return;
    setUpdating(true);
    const updated = await orderService.updateStatus(order.id, newStatus);
    setOrder(updated);
    showToast(`Order status updated to "${orderStatusLabel(newStatus)}"`);
    setUpdating(false);
  }

  if (!order) {
    return (
      <>
        <AdminHeader title="Order Details" onMenuClick={onMenuClick} />
        <div className="p-6">
          <div className="h-64 bg-rs-surface animate-pulse" />
        </div>
      </>
    );
  }

  return (
    <>
      <AdminHeader
        title={`Order ${order.orderNumber}`}
        onMenuClick={onMenuClick}
        actions={
          <Link to="/admin/orders" className="text-sm text-rs-muted hover:text-rs-ink flex items-center gap-1 transition-colors">
            <ArrowLeft size={14} /> All Orders
          </Link>
        }
      />

      <main className="p-4 md:p-6">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-5">
          {/* Left: Items + timeline */}
          <div className="lg:col-span-2 space-y-5">
            {/* Order items */}
            <div className="bg-white border border-rs-border">
              <div className="px-5 py-4 border-b border-rs-border flex items-center justify-between">
                <h2 className="font-display font-semibold text-rs-ink">Items</h2>
                <Badge variant={orderStatusBadgeVariant(order.status)}>{orderStatusLabel(order.status)}</Badge>
              </div>
              <div className="divide-y divide-rs-border">
                {order.items.map(({ product, quantity, unitPrice }) => (
                  <div key={product.id} className="flex gap-4 p-5">
                    <img src={product.images[0]} alt={product.name} className="w-14 h-[75px] object-cover bg-rs-surface shrink-0" />
                    <div className="flex-1 min-w-0">
                      <p className="font-semibold text-rs-ink text-sm">{product.name}</p>
                      <p className="text-xs text-rs-muted mt-0.5">{product.sku} · {product.length} · {product.color}</p>
                      <p className="text-xs text-rs-muted mt-0.5">Qty: {quantity} × {formatPrice(unitPrice)}</p>
                    </div>
                    <span className="text-sm font-semibold text-rs-ink shrink-0">{formatPrice(unitPrice * quantity)}</span>
                  </div>
                ))}
              </div>

              {/* Totals */}
              <div className="px-5 py-4 border-t border-rs-border space-y-2 text-sm bg-rs-surface/40">
                <div className="flex justify-between text-rs-muted"><span>Subtotal</span><span>{formatPrice(order.subtotal)}</span></div>
                <div className="flex justify-between text-rs-muted">
                  <span className="flex items-center gap-1"><Truck size={12} /> Delivery</span>
                  <span>{order.deliveryFee === 0 ? 'Free' : formatPrice(order.deliveryFee)}</span>
                </div>
                <div className="flex justify-between font-semibold text-rs-ink border-t border-rs-border pt-2">
                  <span>Total (COD)</span>
                  <span className="text-base">{formatPrice(order.total)}</span>
                </div>
              </div>
            </div>

            {/* Status update */}
            <div className="bg-white border border-rs-border p-5">
              <h2 className="font-display font-semibold text-rs-ink mb-4">Update Order Status</h2>
              <div className="flex gap-3">
                <Select
                  options={statusOptions}
                  value={newStatus}
                  onChange={(e) => setNewStatus(e.target.value as OrderStatus)}
                  className="flex-1"
                />
                <Button
                  onClick={handleStatusUpdate}
                  loading={updating}
                  disabled={newStatus === order.status}
                  size="md"
                >
                  Update
                </Button>
              </div>
              <p className="text-xs text-rs-muted mt-2">
                Last updated: {formatDateTime(order.updatedAt)}
              </p>
            </div>
          </div>

          {/* Right: Customer + Delivery */}
          <div className="space-y-5">
            {/* Customer info */}
            <div className="bg-white border border-rs-border">
              <div className="px-5 py-4 border-b border-rs-border">
                <h2 className="font-display font-semibold text-rs-ink">Customer</h2>
              </div>
              <div className="p-5 space-y-3 text-sm">
                <p className="font-semibold text-rs-ink">{order.customer.fullName}</p>
                <a href={`tel:${order.customer.phone}`} className="flex items-center gap-2 text-rs-muted hover:text-rs-ink transition-colors">
                  <Phone size={13} className="text-rs-accent" />{order.customer.phone}
                </a>
                <a href={`mailto:${order.customer.email}`} className="flex items-center gap-2 text-rs-muted hover:text-rs-ink transition-colors">
                  <Mail size={13} className="text-rs-accent" />{order.customer.email}
                </a>
              </div>
            </div>

            {/* Delivery info */}
            <div className="bg-white border border-rs-border">
              <div className="px-5 py-4 border-b border-rs-border">
                <h2 className="font-display font-semibold text-rs-ink">Delivery Address</h2>
              </div>
              <div className="p-5 space-y-2 text-sm text-rs-muted">
                <div className="flex items-start gap-2">
                  <MapPin size={13} className="mt-0.5 text-rs-accent shrink-0" />
                  <span>{order.delivery.address}, {order.delivery.city}, {order.delivery.region}</span>
                </div>
                {order.delivery.notes && (
                  <p className="text-xs italic border-t border-rs-border pt-2 mt-2">
                    Note: {order.delivery.notes}
                  </p>
                )}
              </div>
            </div>

            {/* Order meta */}
            <div className="bg-white border border-rs-border p-5 space-y-3 text-sm">
              <h2 className="font-display font-semibold text-rs-ink mb-2">Order Info</h2>
              <div className="flex justify-between text-rs-muted">
                <span>Order #</span>
                <span className="font-mono font-semibold text-rs-ink">{order.orderNumber}</span>
              </div>
              <div className="flex justify-between text-rs-muted">
                <span>Placed</span>
                <span className="text-rs-ink">{formatDate(order.createdAt)}</span>
              </div>
              <div className="flex justify-between text-rs-muted">
                <span>Payment</span>
                <span className="text-rs-ink font-medium">Cash on Delivery</span>
              </div>
            </div>
          </div>
        </div>
      </main>
    </>
  );
}
