import { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { ChevronLeft } from 'lucide-react';
import { ProductGallery } from '../../components/products/ProductGallery';
import { ProductInfo } from '../../components/products/ProductInfo';
import { ProductCard } from '../../components/products/ProductCard';
import { productService } from '../../services/productService';
import type { Product } from '../../types/product';

export default function ProductDetails() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [product, setProduct] = useState<Product | null>(null);
  const [related, setRelated] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    productService.getById(id).then((p) => {
      if (!p) { setNotFound(true); setLoading(false); return; }
      setProduct(p);
      // Fetch related products (same category, different ID)
      productService.getAll().then((all) => {
        const rel = all.filter(
          (r) => r.id !== p.id && r.category === p.category && r.status === 'active'
        ).slice(0, 4);
        setRelated(rel);
      });
      setLoading(false);
    });
  }, [id]);

  if (loading) {
    return (
      <div className="page-container py-12">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-10">
          <div className="aspect-[3/4] bg-rs-surface animate-pulse" />
          <div className="space-y-4">
            <div className="h-6 w-24 bg-rs-surface animate-pulse" />
            <div className="h-10 w-3/4 bg-rs-surface animate-pulse" />
            <div className="h-8 w-1/3 bg-rs-surface animate-pulse" />
            <div className="h-20 bg-rs-surface animate-pulse mt-4" />
          </div>
        </div>
      </div>
    );
  }

  if (notFound || !product) {
    return (
      <div className="page-container py-20 text-center">
        <h2 className="font-display text-3xl font-bold text-rs-ink mb-3">Product Not Found</h2>
        <p className="text-rs-muted mb-6">This product doesn't exist or has been removed.</p>
        <Link to="/shop" className="btn-primary inline-flex">Back to Shop</Link>
      </div>
    );
  }

  return (
    <div className="page-container py-8">
      {/* Breadcrumb */}
      <nav className="flex items-center gap-2 mb-8 text-sm text-rs-muted" aria-label="Breadcrumb">
        <button onClick={() => navigate(-1)} className="flex items-center gap-1 hover:text-rs-ink transition-colors">
          <ChevronLeft size={14} /> Back
        </button>
        <span>/</span>
        <Link to="/shop" className="hover:text-rs-ink transition-colors">Shop</Link>
        <span>/</span>
        <span className="text-rs-ink truncate max-w-xs">{product.name}</span>
      </nav>

      {/* Product */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-10 lg:gap-16">
        <ProductGallery images={product.images} name={product.name} />
        <ProductInfo product={product} />
      </div>

      {/* Related Products */}
      {related.length > 0 && (
        <section className="mt-20 border-t border-rs-border pt-14">
          <div className="mb-8">
            <span className="section-label block mb-2">You Might Also Like</span>
            <h2 className="font-display text-2xl font-bold text-rs-ink">Related Products</h2>
          </div>
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-3 md:gap-4">
            {related.map((p) => <ProductCard key={p.id} product={p} />)}
          </div>
        </section>
      )}
    </div>
  );
}
