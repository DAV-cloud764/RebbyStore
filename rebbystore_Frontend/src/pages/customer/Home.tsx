import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, Star, Zap, Heart } from 'lucide-react';
import { ProductCard } from '../../components/products/ProductCard';
import { Button } from '../../components/ui/Button';
import { productService } from '../../services/productService';
import type { Product } from '../../types/product';
import { storeConfig } from '../../config/store';


// ─── Bento Grid ───────────────────────────────────────────────────────────────

function BentoGrid() {
  return (
    <section className="page-container py-6" aria-label="Hero">
      {/* Desktop 5-col bento */}
      <div className="hidden md:grid grid-cols-5 gap-3" style={{ gridTemplateRows: '280px 280px 220px' }}>

        {/* Primary Hero — col 1-3, rows 1-2 */}
        <div className="col-span-3 row-span-2 relative overflow-hidden bg-rs-ink group cursor-pointer">
          <img
            src="/public/images/products/Premium-hero.webp"
            alt=""
            className="absolute inset-0 w-full h-full object-cover opacity-30 group-hover:scale-105 transition-transform duration-700"
            aria-hidden="true"
          />
          <div className="absolute inset-0 bg-linear-to-t from-rs-ink via-rs-ink/60 to-transparent" />
          <div className="relative h-full flex flex-col justify-end p-10">
            <span className="section-label text-rs-accent mb-4">Premium Collection · 2026</span>
            <h1 className="font-display text-5xl xl:text-6xl font-bold text-white leading-[1.05] mb-5">
              Your Crown.<br />Your Style.
            </h1>
            <p className="text-white/65 text-sm leading-relaxed max-w-xs mb-8">
              Premium wigs crafted for the modern Tanzanian woman. Wear your confidence every single day.
            </p>
            <div className="flex gap-3">
              <Link to="/shop">
                <Button variant="accent" size="md">Shop Collection</Button>
              </Link>
              <Link to="/shop?isNew=true">
                <Button variant="outline-white" size="md">New Arrivals</Button>
              </Link>
            </div>
          </div>
        </div>

        {/* New Arrivals — col 4-5, row 1 */}
        <div className="col-span-2 relative overflow-hidden bg-rs-surface group">
          <img
             src="/public/images/products/deep-curly-22.webp"
              alt=""
             className="absolute inset-0 w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
          aria-hidden="true"
          />

          <div
             className="absolute inset-0 bg-black/45"
             aria-hidden="true"
         />
          <div className="relative h-full flex flex-col justify-between p-6">
            <span className="section-label">New Arrivals</span>
            <div>
              <h2 className="font-display text-2xl font-semibold text-white mb-2">Just Landed</h2>
              <p className="text-xs text-white/80 mb-4">Fresh styles added this season.</p>
              <Link to="/shop?isNew=true" className="inline-flex items-center gap-1.5 text-xs font-semibold text-white hover:text-rs-accent transition-colors">
                Explore <ArrowRight size={13} />
              </Link>
            </div>
          </div>
        </div>

        {/* Featured Collection — col 4-5, row 2 */}
        <div className="col-span-2 relative overflow-hidden bg-rs-accent group">
          <div className="relative h-full flex flex-col justify-between p-6">
            <span className="section-label text-white/80">Featured</span>
            <div>
              <h2 className="font-display text-2xl font-semibold text-white mb-2">Human Hair</h2>
              <p className="text-xs text-white/70 mb-4">Natural look. Lasting quality.</p>
              <Link to="/shop?category=human-hair" className="inline-flex items-center gap-1.5 text-xs font-semibold text-white hover:text-white/80 transition-colors">
                Shop Now <ArrowRight size={13} />
              </Link>
            </div>
          </div>
        </div>

        {/* Category Tile — col 1-2, row 3 */}
<div className="col-span-2 relative overflow-hidden group min-h-70">
  <img
    src="/public/images/products/deep-curly-22.webp"
    alt="Curly Collection"
    className="absolute inset-0 w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
  />

  {/* Gradient overlay for readability */}
  <div
    className="absolute inset-0 bg-linear-to-t from-black/75 via-black/30 to-black/5"
    aria-hidden="true"
  />

  <div className="relative z-10 h-full flex flex-col justify-between p-6">
    <span className="section-label text-white/90">
      Category
    </span>

    <div>
      <h2 className="font-display text-2xl font-semibold text-white mb-2">
        Curly Collection
      </h2>

      <Link
        to="/shop?texture=curly"
        className="inline-flex items-center gap-1.5 text-xs font-semibold text-white hover:text-white/80 transition-colors"
      >
        Browse <ArrowRight size={13} />
      </Link>
    </div>
  </div>
</div>

        {/* Promotional — col 3-5, row 3 */}
        <div className="col-span-3 relative overflow-hidden bg-rs-ink/90 group">
          <img
            src="/public/images/products/Delivery.webp"
            alt=""
            className="absolute inset-0 w-full h-full object-cover opacity-20 group-hover:scale-105 transition-transform duration-500"
            aria-hidden="true"
          />
          <div className="relative h-full flex flex-col justify-between p-6">
            <span className="section-label text-rs-accent">Offer</span>
            <div className="flex items-end justify-between">
              <div>
                <h2 className="font-display text-2xl font-semibold text-white mb-2">Free Delivery</h2>
                <p className="text-xs text-white/60">On orders above {storeConfig.currency} {(storeConfig.freeDeliveryThreshold).toLocaleString()}</p>
              </div>
              <Link to="/shop">
                <Button variant="accent" size="sm">Shop & Save</Button>
              </Link>
            </div>
          </div>
        </div>

      </div>

      {/* Mobile bento — stacked */}
      <div className="md:hidden flex flex-col gap-3">
        {/* Hero */}
        <div className="relative overflow-hidden bg-rs-ink" style={{ height: 380 }}>
          <img src="https://picsum.photos/seed/rebby-hero/600/400" alt="" className="absolute inset-0 w-full h-full object-cover opacity-30" aria-hidden="true" />
          <div className="absolute inset-0 bg-iner-to-t from-rs-ink via-rs-ink/50 to-transparent" />
          <div className="relative h-full flex flex-col justify-end p-6">
            <span className="section-label text-rs-accent mb-3">Premium Collection</span>
            <h1 className="font-display text-4xl font-bold text-white leading-tight mb-4">
              Your Crown.<br />Your Style.
            </h1>
            <div className="flex gap-3">
              <Link to="/shop"><Button variant="accent" size="sm">Shop Now</Button></Link>
            </div>
          </div>
        </div>

        {/* Quick tiles */}
        <div className="grid grid-cols-2 gap-3">
          <Link to="/shop?isNew=true" className="bg-rs-surface p-5 flex flex-col gap-2">
            <span className="section-label">New</span>
            <p className="font-display font-semibold text-rs-ink">New Arrivals</p>
          </Link>
          <Link to="/shop?category=human-hair" className="bg-rs-accent p-5 flex flex-col gap-2">
            <span className="section-label text-white/60">Featured</span>
            <p className="font-display font-semibold text-white">Human Hair</p>
          </Link>
        </div>
      </div>
    </section>
  );
}

// ─── Featured Categories ──────────────────────────────────────────────────────

const categories = [
  {
    id: 'human-hair',
    label: 'Human Hair',
    description: 'Natural. Versatile. Lasting.',
    img: '/public/images/products/Human-hair.webp',
  },
  {
    id: 'lace-front',
    label: 'Lace Front',
    description: 'Seamless, natural hairlines.',
    img: '/public/images/products/straight-bob-12.webp',
  },
  {
    id: 'bob',
    label: 'Bob Styles',
    description: 'Chic cuts for every mood.',
    img: '/public/images/products/Water-Wave-24.webp',
  },
  {
    id: 'closure',
    label: 'Closure',
    description: 'Full and natural coverage.',
    img: '/public/images/products/deep-curly-22.webp',
  },
];

function FeaturedCategories() {
  return (
    <section className="page-container py-14">
      <div className="flex items-end justify-between mb-8">
        <div>
          <span className="section-label block mb-2">Collections</span>
          <h2 className="font-display text-3xl font-bold text-rs-ink">Shop by Category</h2>
        </div>
        <Link to="/shop" className="hidden sm:flex items-center gap-1.5 text-sm text-rs-muted hover:text-rs-ink transition-colors">
          View all <ArrowRight size={14} />
        </Link>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
        {categories.map((cat) => (
          <Link
  key={cat.id}
  to={`/shop?category=${cat.id}`}
  className="group relative overflow-hidden bg-rs-surface aspect-3/4"
>
  <img
    src={cat.img}
    alt={cat.label}
    className="absolute inset-0 w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
  />

  <div
    className="absolute inset-0 bg-linear-to-t from-black/75 via-black/25 to-transparent"
    aria-hidden="true"
  />

  <div className="relative z-10 h-full flex flex-col justify-end p-5">
    <h3 className="font-display font-semibold text-white text-lg">
      {cat.label}
    </h3>

    <p className="text-xs text-white/75 mt-1">
      {cat.description}
    </p>
  </div>
</Link>
        ))}
      </div>
    </section>
  );
}

// ─── Product Section ──────────────────────────────────────────────────────────

function ProductSection({ title, label, products, viewAllLink }: {
  title: string;
  label: string;
  products: Product[];
  viewAllLink: string;
}) {
  return (
    <section className="page-container py-14 border-t border-rs-border">
      <div className="flex items-end justify-between mb-8">
        <div>
          <span className="section-label block mb-2">{label}</span>
          <h2 className="font-display text-3xl font-bold text-rs-ink">{title}</h2>
        </div>
        <Link to={viewAllLink} className="hidden sm:flex items-center gap-1.5 text-sm text-rs-muted hover:text-rs-ink transition-colors">
          View all <ArrowRight size={14} />
        </Link>
      </div>
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-3 md:gap-4">
        {products.map((p) => <ProductCard key={p.id} product={p} />)}
      </div>
    </section>
  );
}

// ─── Why Shop With Us ─────────────────────────────────────────────────────────

function WhyShopWithUs() {
  const points = [
    { icon: <Star size={22} className="text-rs-accent" />, title: 'Premium Quality', desc: 'Every wig is carefully sourced and quality-checked before it reaches you.' },
    { icon: <Zap size={22} className="text-rs-accent" />, title: 'Fast Delivery', desc: 'Same-day delivery available in Dar es Salaam. Nationwide shipping.' },
    { icon: <Heart size={22} className="text-rs-accent" />, title: 'Expert Support', desc: 'Our team knows wigs. We help you find your perfect match.' },
  ];

  return (
    <section className="bg-rs-surface py-16">
      <div className="page-container">
        <div className="text-center mb-12">
          <span className="section-label block mb-2">Our Promise</span>
          <h2 className="font-display text-3xl font-bold text-rs-ink">Why Choose RebbyStore</h2>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {points.map(({ icon, title, desc }) => (
            <div key={title} className="flex flex-col items-center text-center gap-4">
              <div className="w-12 h-12 bg-white border border-rs-border flex items-center justify-center">
                {icon}
              </div>
              <div>
                <h3 className="font-display font-semibold text-rs-ink mb-1">{title}</h3>
                <p className="text-sm text-rs-muted leading-relaxed">{desc}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

// ─── Lookbook Banner ──────────────────────────────────────────────────────────

function LookbookBanner() {
  return (
    <section className="page-container py-14 border-t border-rs-border">
      <div className="relative overflow-hidden bg-rs-ink" style={{ minHeight: 320 }}>
        <img
          src="https://picsum.photos/seed/rebby-lookbook/1400/400"
          alt=""
          className="absolute inset-0 w-full h-full object-cover opacity-20"
          aria-hidden="true"
        />
        <div className="relative flex flex-col items-center justify-center text-center p-12 gap-4" style={{ minHeight: 320 }}>
          <span className="section-label text-rs-accent">Lookbook</span>
          <h2 className="font-display text-4xl font-bold text-white max-w-md">
            Wear Your Crown With Pride
          </h2>
          <p className="text-white/60 text-sm max-w-xs">
            Follow us on Instagram for styling tips, new arrivals, and customer looks.
          </p>
          <a href={storeConfig.social.instagram} target="_blank" rel="noopener noreferrer">
            <Button variant="outline-white" size="md">@RebbyStore on Instagram</Button>
          </a>
        </div>
      </div>
    </section>
  );
}

// ─── Page ─────────────────────────────────────────────────────────────────────

export default function Home() {
  const [newArrivals, setNewArrivals] = useState<Product[]>([]);
  const [featured, setFeatured] = useState<Product[]>([]);

  useEffect(() => {
    productService.getNewArrivals().then((p) => setNewArrivals(p.slice(0, 4)));
    productService.getFeatured().then((p) => setFeatured(p.slice(0, 4)));
  }, []);

  return (
    <>
      <BentoGrid />
      <FeaturedCategories />
      {newArrivals.length > 0 && (
        <ProductSection title="New Arrivals" label="Fresh Drops" products={newArrivals} viewAllLink="/shop?isNew=true" />
      )}
      {featured.length > 0 && (
        <ProductSection title="Featured Collection" label="Our Picks" products={featured} viewAllLink="/shop" />
      )}
      <WhyShopWithUs />
      <LookbookBanner />
    </>
  );
}
