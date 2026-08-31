import { Link } from 'react-router-dom';
import { useCart } from '../../contexts/CartContext';
import { Button } from '../ui/Button';
import { formatPrice } from '../../utils/formatting';
import { storeConfig } from '../../config/store';
import { Truck } from 'lucide-react';

interface CartSummaryProps {
  showCheckoutButton?: boolean;
}

export function CartSummary({ showCheckoutButton = true }: CartSummaryProps) {
  const { total, itemCount } = useCart();
  const deliveryFee = total >= storeConfig.freeDeliveryThreshold ? 0 : storeConfig.deliveryFee;
  const orderTotal = total + deliveryFee;

  return (
    <div className="bg-rs-surface p-6">
      <h2 className="font-display font-semibold text-rs-ink mb-5">Order Summary</h2>

      <div className="space-y-3 text-sm">
        <div className="flex justify-between">
          <span className="text-rs-muted">Subtotal ({itemCount} item{itemCount !== 1 ? 's' : ''})</span>
          <span className="text-rs-ink font-medium">{formatPrice(total)}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-rs-muted flex items-center gap-1.5">
            <Truck size={13} /> Delivery
          </span>
          <span className={deliveryFee === 0 ? 'text-green-600 font-medium' : 'text-rs-ink font-medium'}>
            {deliveryFee === 0 ? 'Free' : formatPrice(deliveryFee)}
          </span>
        </div>

        {total < storeConfig.freeDeliveryThreshold && (
          <p className="text-[11px] text-rs-muted border border-rs-border px-3 py-2">
            Add {formatPrice(storeConfig.freeDeliveryThreshold - total)} more for free delivery
          </p>
        )}

        <div className="border-t border-rs-border pt-3 flex justify-between font-semibold text-rs-ink">
          <span>Total</span>
          <span className="text-lg">{formatPrice(orderTotal)}</span>
        </div>
      </div>

      {/* Payment badge */}
      <div className="mt-4 px-3 py-2 border border-rs-border bg-white text-xs text-rs-muted flex items-center gap-2">
        <span className="w-2 h-2 rounded-full bg-green-500 shrink-0" />
        Payment: <span className="font-medium text-rs-ink">Cash on Delivery</span>
      </div>

      {showCheckoutButton && (
        <Link to="/checkout" className="block mt-5">
          <Button variant="primary" size="lg" className="w-full justify-center">
            Proceed to Checkout
          </Button>
        </Link>
      )}

      <Link to="/shop" className="block text-center text-xs text-rs-muted hover:text-rs-ink transition-colors mt-4">
        Continue Shopping
      </Link>
    </div>
  );
}
