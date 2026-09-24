import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { CheckCircle, MapPin, Phone, Mail, Truck } from 'lucide-react';
import { orderService } from '../../services/orderService';
import type { Order } from '../../types/order';
import { formatPrice, formatDate } from '../../utils/formatting';
import heroImage from '../../assets/hero.png';

export default function OrderConfirmation() {
  const { id } = useParams<{ id: string }>();
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    orderService.getById(id).then((o) => {
      setOrder(o ?? null);
      setLoading(false);
    });
  }, [id]);

  if (loading) {
    return (
      <div className="page-container py-20 text-center">
        <div className="w-12 h-12 border-2 border-rs-ink border-t-transparent rounded-full animate-spin mx-auto" />
      </div>
    );
  }

  if (!order) {
    return (
      <div className="page-container py-20 text-center">
        <h2 className="font-display text-2xl font-bold text-rs-ink mb-4">Order not found</h2>
        <Link to="/" className="btn-primary inline-flex">Back to Home</Link>
      </div>
    );
  }

  return (
    <div className="page-container py-12 max-w-2xl">
      {/* Header */}
      <div className="text-center mb-10">
        <CheckCircle size={56} className="text-green-500 mx-auto mb-4" />
        <h1 className="font-display text-4xl font-bold text-rs-ink mb-2">Order Confirmed!</h1>
        <p className="text-rs-muted">
          Thank you, {order.customer.fullName}. Your order has been received.
        </p>
        <div className="inline-block mt-4 px-4 py-2 bg-rs-surface border border-rs-border text-sm">
          Order Number: <span className="font-mono font-semibold text-rs-ink">{order.orderNumber}</span>
        </div>
      </div>

      {/* Order Items */}
      <section className="border border-rs-border bg-white mb-5">
        <div className="px-5 py-4 border-b border-rs-border">
          <h2 className="font-display font-semibold text-rs-ink">Items Ordered</h2>
        </div>
        <div className="divide-y divide-rs-border">
          {order.items.map(
  ({ id, productName, sku, quantity, subtotal }) => (
    <div
      key={id ?? `${sku}-${productName}`}
      className="flex gap-4 p-5"
    >
      <img
        src={heroImage}
        alt={productName}
        className="w-16 h-[86px] object-cover bg-rs-surface shrink-0"
      />

      <div className="flex-1 min-w-0">
        <p className="font-semibold text-rs-ink text-sm">
          {productName}
        </p>

        <p className="text-xs text-rs-muted mt-0.5">
          {sku}
        </p>

        <p className="text-xs text-rs-muted mt-0.5">
          Qty: {quantity}
        </p>
      </div>

      <span className="text-sm font-semibold text-rs-ink shrink-0">
        {formatPrice(subtotal)}
      </span>
    </div>
  )
)}
        </div>

        {/* Totals */}
        <div className="px-5 py-4 border-t border-rs-border space-y-2 text-sm">
          <div className="flex justify-between text-rs-muted">
            <span>Subtotal</span><span>{formatPrice(order.subtotal)}</span>
          </div>
          <div className="flex justify-between text-rs-muted">
            <span className="flex items-center gap-1"><Truck size={12} /> Delivery</span>
            <span>{order.deliveryFee === 0 ? 'Free' : formatPrice(order.deliveryFee)}</span>
          </div>
          <div className="flex justify-between font-semibold text-rs-ink border-t border-rs-border pt-2">
            <span>Total</span><span className="text-lg">{formatPrice(order.total)}</span>
          </div>
        </div>
      </section>

      {/* Delivery info */}
      <section className="border border-rs-border bg-white mb-5">
        <div className="px-5 py-4 border-b border-rs-border">
          <h2 className="font-display font-semibold text-rs-ink">Delivery Information</h2>
        </div>
        <div className="p-5 space-y-3 text-sm">
          <div className="flex items-start gap-2 text-rs-muted">
            <MapPin size={14} className="mt-0.5 shrink-0 text-rs-accent" />
            <span>{order.delivery.address}, {order.delivery.city}, {order.delivery.region}</span>
          </div>
          <div className="flex items-center gap-2 text-rs-muted">
            <Phone size={14} className="text-rs-accent" />{order.customer.phone}
          </div>
          <div className="flex items-center gap-2 text-rs-muted">
            <Mail size={14} className="text-rs-accent" />{order.customer.email}
          </div>
          {order.delivery.notes && (
            <p className="text-rs-muted italic">Note: {order.delivery.notes}</p>
          )}
        </div>
      </section>

      {/* Payment */}
      <section className="border border-rs-border bg-rs-surface p-5 mb-8">
        <h2 className="font-display font-semibold text-rs-ink mb-2">Payment Method</h2>
        <div className="flex items-center gap-2 text-sm">
          <span className="w-2 h-2 rounded-full bg-green-500" />
          <span className="font-semibold text-rs-ink">Cash on Delivery</span>
        </div>
        <p className="text-xs text-rs-muted mt-2">
          Please have <strong>{formatPrice(order.total)}</strong> ready to pay when your order is delivered.
          Our agent will contact you on <strong>{order.customer.phone}</strong> to arrange delivery.
        </p>
        <p className="text-[11px] text-rs-muted mt-2">
          Order placed on {formatDate(order.createdAt)}.
        </p>
      </section>

      {/* CTA */}
      <div className="text-center">
        <Link to="/shop" className="btn-primary inline-flex px-8 py-4">Continue Shopping</Link>
      </div>
    </div>
  );
}
