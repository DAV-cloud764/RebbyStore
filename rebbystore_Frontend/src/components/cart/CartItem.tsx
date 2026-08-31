import { Link } from 'react-router-dom';
import { Minus, Plus, X } from 'lucide-react';
import type { CartItem as CartItemType } from '../../types/order';
import { useCart } from '../../contexts/CartContext';
import { formatPrice } from '../../utils/formatting';

interface CartItemProps { item: CartItemType; }

export function CartItem({ item }: CartItemProps) {
  const { updateQuantity, removeItem } = useCart();
  const { product, quantity } = item;

  return (
    <div className="flex gap-4 py-5 border-b border-rs-border last:border-0">
      {/* Image */}
      <Link to={`/product/${product.id}`} className="shrink-0">
        <img src={product.images[0]} alt={product.name} className="w-20 h-[108px] object-cover bg-rs-surface" />
      </Link>

      {/* Details */}
      <div className="flex-1 min-w-0">
        <div className="flex items-start justify-between gap-2">
          <div>
            <p className="section-label mb-0.5">{product.category.replace('-', ' ')}</p>
            <Link to={`/product/${product.id}`} className="font-display font-semibold text-rs-ink hover:text-rs-accent transition-colors text-sm leading-snug">
              {product.name}
            </Link>
            <p className="text-xs text-rs-muted mt-0.5">{product.length} · {product.color}</p>
          </div>
          <button onClick={() => removeItem(product.id)} className="text-rs-muted hover:text-rs-ink transition-colors shrink-0 mt-0.5" aria-label={`Remove ${product.name}`}>
            <X size={15} />
          </button>
        </div>

        <div className="flex items-center justify-between mt-3">
          {/* Quantity */}
          <div className="flex items-center border border-rs-border">
            <button
              onClick={() => updateQuantity(product.id, quantity - 1)}
              disabled={quantity <= 1}
              className="w-8 h-8 flex items-center justify-center text-rs-muted hover:text-rs-ink disabled:opacity-30 transition-colors"
              aria-label="Decrease"
            >
              <Minus size={12} />
            </button>
            <span className="w-8 text-center text-sm font-medium text-rs-ink">{quantity}</span>
            <button
              onClick={() => updateQuantity(product.id, quantity + 1)}
              disabled={quantity >= product.stockQuantity}
              className="w-8 h-8 flex items-center justify-center text-rs-muted hover:text-rs-ink disabled:opacity-30 transition-colors"
              aria-label="Increase"
            >
              <Plus size={12} />
            </button>
          </div>

          {/* Price */}
          <div className="text-right">
            <p className="text-sm font-semibold text-rs-ink">{formatPrice(product.price * quantity)}</p>
            {quantity > 1 && <p className="text-[11px] text-rs-muted">{formatPrice(product.price)} each</p>}
          </div>
        </div>

        {/* Stock warning */}
        {product.stockQuantity <= product.lowStockThreshold && product.stockQuantity > 0 && (
          <p className="text-[11px] text-amber-600 mt-2">Only {product.stockQuantity} in stock</p>
        )}
      </div>
    </div>
  );
}
