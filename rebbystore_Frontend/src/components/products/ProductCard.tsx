import { Link } from 'react-router-dom';
import { Heart, ShoppingBag } from 'lucide-react';
import { getStockStatus } from '../../types/product';
import type { Product } from '../../types/product';
import { Badge } from '../ui/Badge';
import { useCart } from '../../contexts/CartContext';
import { useWishlist } from '../../contexts/WishlistContext';
import { useToast } from '../../contexts/ToastContext';
import { formatPrice } from '../../utils/formatting';

interface ProductCardProps {
  product: Product;
}

export function ProductCard({ product }: ProductCardProps) {
  const { addItem, isInCart } = useCart();
  const { toggle, isWishlisted } = useWishlist();
  const { showToast } = useToast();
  const stockStatus = getStockStatus(product);

  function handleAddToCart(e: React.MouseEvent) {
    e.preventDefault();
    e.stopPropagation();
    if (stockStatus === 'out-of-stock') return;
    addItem(product, 1);
    showToast(`${product.name} added to cart`);
  }

  function handleWishlist(e: React.MouseEvent) {
    e.preventDefault();
    e.stopPropagation();
    toggle(product.id);
    showToast(isWishlisted(product.id) ? 'Removed from wishlist' : 'Added to wishlist', 'info');
  }

  const wishlisted = isWishlisted(product.id);
  const inCart = isInCart(product.id);

  return (
    <article className="group relative bg-white border border-rs-border/60 hover:border-rs-border transition-colors duration-200">
      {/* Image */}
      <Link to={`/product/${product.id}`} className="block relative overflow-hidden aspect-[3/4] bg-rs-surface" aria-label={`View ${product.name}`}>
        <img
          src={product.images[0]}
          alt={product.name}
          className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
          loading="lazy"
        />

        {/* Badges top-left */}
        <div className="absolute top-3 left-3 flex flex-col gap-1.5">
          {product.isNew && <Badge variant="new">New</Badge>}
          {product.originalPrice && <Badge variant="sale">Sale</Badge>}
          {stockStatus === 'out-of-stock' && <Badge variant="out-of-stock">Out of Stock</Badge>}
          {stockStatus === 'low-stock' && (
            <Badge variant="low-stock">Only {product.stockQuantity} left</Badge>
          )}
        </div>

        {/* Wishlist button top-right */}
        <button
          onClick={handleWishlist}
          className="absolute top-3 right-3 w-8 h-8 bg-white/90 backdrop-blur-sm flex items-center justify-center transition-all duration-200 hover:bg-white opacity-0 group-hover:opacity-100"
          aria-label={wishlisted ? 'Remove from wishlist' : 'Add to wishlist'}
        >
          <Heart
            size={15}
            className={wishlisted ? 'fill-rs-accent text-rs-accent' : 'text-rs-muted'}
          />
        </button>

        {/* Quick add — revealed on hover */}
        {stockStatus !== 'out-of-stock' && (
          <div className="absolute bottom-0 left-0 right-0 translate-y-full group-hover:translate-y-0 transition-transform duration-300">
            <button
              onClick={handleAddToCart}
              className={`w-full py-3 text-xs font-semibold tracking-widest uppercase flex items-center justify-center gap-2 transition-colors ${
                inCart
                  ? 'bg-rs-accent text-white'
                  : 'bg-rs-ink text-white hover:bg-rs-accent'
              }`}
            >
              <ShoppingBag size={13} />
              {inCart ? 'In Cart' : 'Quick Add'}
            </button>
          </div>
        )}

        {/* Overlay for out of stock */}
        {stockStatus === 'out-of-stock' && (
          <div className="absolute inset-0 bg-white/50 backdrop-blur-[1px]" />
        )}
      </Link>

      {/* Info */}
      <div className="p-3">
        <p className="section-label text-[10px] mb-1">{product.category.replace('-', ' ')}</p>
        <Link to={`/product/${product.id}`}>
          <h3 className="font-display font-semibold text-rs-ink text-sm leading-tight hover:text-rs-accent transition-colors line-clamp-1">
            {product.name}
          </h3>
        </Link>
        <div className="flex items-center gap-2 mt-1.5">
          <span className="text-sm font-semibold text-rs-ink">{formatPrice(product.price)}</span>
          {product.originalPrice && (
            <span className="text-xs text-rs-muted line-through">{formatPrice(product.originalPrice)}</span>
          )}
        </div>
      </div>
    </article>
  );
}
