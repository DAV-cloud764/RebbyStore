import { useState } from 'react';
import { ShoppingBag, Heart, Minus, Plus, Truck, RotateCcw, Shield } from 'lucide-react';
import { getStockStatus } from '../../types/product';
import type { Product } from '../../types/product';
import { Badge } from '../ui/Badge';
import { Button } from '../ui/Button';
import { useCart } from '../../contexts/CartContext';
import { useWishlist } from '../../contexts/WishlistContext';
import { useToast } from '../../contexts/ToastContext';
import { formatPrice } from '../../utils/formatting';

interface ProductInfoProps {
  product: Product;
}

export function ProductInfo({ product }: ProductInfoProps) {
  const { addItem, isInCart, getQuantity } = useCart();
  const { toggle, isWishlisted } = useWishlist();
  const { showToast } = useToast();
  const [quantity, setQuantity] = useState(1);

  const stockStatus = getStockStatus(product);
  const wishlisted = isWishlisted(product.id);
  const inCart = isInCart(product.id);
  const cartQty = getQuantity(product.id);
  const maxQty = product.stockQuantity - cartQty;

  function handleAddToCart() {
    if (stockStatus === 'out-of-stock') return;
    addItem(product, quantity);
    showToast(`${product.name} added to cart`);
    setQuantity(1);
  }

  function handleWishlist() {
    toggle(product.id);
    showToast(wishlisted ? 'Removed from wishlist' : 'Saved to wishlist', 'info');
  }

  const specs = [
    { label: 'SKU', value: product.sku },
    { label: 'Hair Type', value: product.hairType === 'human' ? 'Human Hair' : product.hairType === 'synthetic' ? 'Synthetic' : 'Blend' },
    { label: 'Texture', value: product.texture.split('-').map((w) => w[0].toUpperCase() + w.slice(1)).join(' ') },
    { label: 'Length', value: product.length },
    { label: 'Colour', value: product.color },
    { label: 'Category', value: product.category.split('-').map((w) => w[0].toUpperCase() + w.slice(1)).join(' ') },
  ];

  return (
    <div className="flex flex-col gap-6">
      {/* Header */}
      <div>
        <div className="flex gap-2 mb-3">
          {product.isNew && <Badge variant="new">New Arrival</Badge>}
          {product.originalPrice && <Badge variant="sale">Sale</Badge>}
          {stockStatus === 'out-of-stock' && <Badge variant="out-of-stock">Out of Stock</Badge>}
          {stockStatus === 'low-stock' && (
            <Badge variant="low-stock">Only {product.stockQuantity} left</Badge>
          )}
          {stockStatus === 'in-stock' && <Badge variant="in-stock">In Stock</Badge>}
        </div>

        <h1 className="font-display text-3xl md:text-4xl font-bold text-rs-ink leading-tight mb-3">
          {product.name}
        </h1>

        <div className="flex items-baseline gap-3">
          <span className="text-2xl font-semibold text-rs-ink">{formatPrice(product.price)}</span>
          {product.originalPrice && (
            <span className="text-lg text-rs-muted line-through">{formatPrice(product.originalPrice)}</span>
          )}
        </div>
      </div>

      {/* Description */}
      <p className="text-sm text-rs-muted leading-relaxed">{product.description}</p>

      {/* Specifications */}
      <div className="grid grid-cols-2 gap-x-6 gap-y-3 border-t border-b border-rs-border py-5">
        {specs.map(({ label, value }) => (
          <div key={label}>
            <span className="section-label block mb-0.5">{label}</span>
            <span className="text-sm text-rs-ink">{value}</span>
          </div>
        ))}
      </div>

      {/* Quantity + Add to cart */}
      {stockStatus !== 'out-of-stock' && (
        <div className="flex flex-col gap-3">
          <div className="flex items-center gap-4">
            <span className="section-label">Quantity</span>
            <div className="flex items-center border border-rs-border">
              <button
                onClick={() => setQuantity((q) => Math.max(1, q - 1))}
                className="w-10 h-10 flex items-center justify-center text-rs-muted hover:text-rs-ink transition-colors"
                disabled={quantity <= 1}
                aria-label="Decrease quantity"
              >
                <Minus size={14} />
              </button>
              <span className="w-12 text-center text-sm font-medium text-rs-ink">{quantity}</span>
              <button
                onClick={() => setQuantity((q) => Math.min(maxQty, q + 1))}
                className="w-10 h-10 flex items-center justify-center text-rs-muted hover:text-rs-ink transition-colors"
                disabled={quantity >= maxQty}
                aria-label="Increase quantity"
              >
                <Plus size={14} />
              </button>
            </div>
            {inCart && (
              <span className="text-xs text-rs-muted">({cartQty} already in cart)</span>
            )}
          </div>

          <div className="flex gap-3">
            <Button
              variant="primary"
              size="lg"
              className="flex-1"
              onClick={handleAddToCart}
              disabled={maxQty <= 0}
            >
              <ShoppingBag size={16} />
              {maxQty <= 0 ? 'Max quantity reached' : inCart ? 'Add More to Cart' : 'Add to Cart'}
            </Button>
            <button
              onClick={handleWishlist}
              className={`w-12 border flex items-center justify-center transition-colors ${
                wishlisted
                  ? 'border-rs-accent bg-rs-surface text-rs-accent'
                  : 'border-rs-border text-rs-muted hover:border-rs-ink hover:text-rs-ink'
              }`}
              aria-label={wishlisted ? 'Remove from wishlist' : 'Add to wishlist'}
            >
              <Heart size={16} className={wishlisted ? 'fill-rs-accent' : ''} />
            </button>
          </div>
        </div>
      )}

      {stockStatus === 'out-of-stock' && (
        <div className="border border-rs-border p-4 text-sm text-rs-muted">
          This item is currently out of stock. Check back soon or save it to your wishlist.
          <button
            onClick={handleWishlist}
            className="flex items-center gap-2 mt-3 text-rs-ink font-medium hover:text-rs-accent transition-colors"
          >
            <Heart size={15} className={wishlisted ? 'fill-rs-accent text-rs-accent' : ''} />
            {wishlisted ? 'Saved to wishlist' : 'Save to wishlist'}
          </button>
        </div>
      )}

      {/* Trust signals */}
      <div className="grid grid-cols-3 gap-4 border-t border-rs-border pt-5">
        {[
          { icon: <Truck size={16} />, label: 'Delivery', desc: 'Dar es Salaam & nationwide' },
          { icon: <RotateCcw size={16} />, label: 'Returns', desc: 'Contact us within 48 hours' },
          { icon: <Shield size={16} />, label: 'Quality', desc: '100% authentic products' },
        ].map(({ icon, label, desc }) => (
          <div key={label} className="flex flex-col items-center text-center gap-1.5">
            <span className="text-rs-accent">{icon}</span>
            <span className="text-xs font-semibold text-rs-ink">{label}</span>
            <span className="text-[11px] text-rs-muted">{desc}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
