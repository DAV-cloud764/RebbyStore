import { Link } from 'react-router-dom';
import { ShoppingBag } from 'lucide-react';
import { CartItem } from '../../components/cart/CartItem';
import { CartSummary } from '../../components/cart/CartSummary';
import { useCart } from '../../contexts/CartContext';

export default function Cart() {
  const { items, clearCart } = useCart();

  return (
    <div className="page-container py-8">
      <div className="mb-8 flex items-end justify-between">
        <div>
          <span className="section-label block mb-1">Shopping</span>
          <h1 className="font-display text-4xl font-bold text-rs-ink">Your Cart</h1>
        </div>
        {items.length > 0 && (
          <button onClick={clearCart} className="text-xs text-rs-muted hover:text-red-500 transition-colors">
            Clear cart
          </button>
        )}
      </div>

      {items.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-24 text-center">
          <ShoppingBag size={48} className="text-rs-border mb-5" />
          <h2 className="font-display text-2xl font-semibold text-rs-ink mb-2">Your cart is empty</h2>
          <p className="text-sm text-rs-muted mb-8 max-w-xs">Start adding some beautiful wigs to your cart and come back here.</p>
          <Link to="/shop" className="btn-primary">Browse the Shop</Link>
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 lg:gap-12">
          {/* Items */}
          <div className="lg:col-span-2">
            <div className="border border-rs-border bg-white px-4 sm:px-6">
              {items.map((item) => <CartItem key={item.product.id} item={item} />)}
            </div>
          </div>

          {/* Summary */}
          <div className="lg:col-span-1">
            <CartSummary />
          </div>
        </div>
      )}
    </div>
  );
}
