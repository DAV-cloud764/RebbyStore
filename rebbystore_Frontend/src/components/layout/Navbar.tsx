import { useState, useEffect } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { ShoppingBag, Heart, Search, Menu, X } from 'lucide-react';
import { useCart } from '../../contexts/CartContext';
import { useWishlist } from '../../contexts/WishlistContext';
import BrandMark from "../ui/BrandMark";

const navLinks = [
  { to: '/', label: 'Home' },
  { to: '/shop', label: 'Shop' },
];

export function Navbar() {
  const { itemCount } = useCart();
  const { count: wishlistCount } = useWishlist();
  const navigate = useNavigate();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);
  const [searchOpen, setSearchOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    const handler = () => setScrolled(window.scrollY > 20);
    window.addEventListener('scroll', handler, { passive: true });
    return () => window.removeEventListener('scroll', handler);
  }, []);

  // Close mobile nav on route change
  useEffect(() => {
    setMobileOpen(false);
  }, []);

  function handleSearch(e: React.FormEvent) {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/shop?search=${encodeURIComponent(searchQuery.trim())}`);
      setSearchQuery('');
      setSearchOpen(false);
    }
  }

  return (
    <>
      <header
        className={`fixed top-0 left-0 right-0 z-40 transition-all duration-300 ${
          scrolled ? 'bg-white border-b border-rs-border shadow-sm' : 'bg-rs-bg'
        }`}
      >
        <div className="page-container">
          <div className="flex items-center justify-between h-16">
            {/* Logo */}
            <Link
  to="/"
  className="flex items-center gap-3 group"
  aria-label="RebbyStore home"
>
  <BrandMark className="h-11 w-11 shrink-0 border-2 border-white shadow-sm" />

  <div className="flex flex-col leading-none">
    <span className="font-display text-[1.35rem] font-semibold tracking-[0.01em] text-rs-ink group-hover:text-rs-accent transition-colors">
      Rebby<span className="text-rs-accent">Store</span>
    </span>

    <span className="mt-1 text-[9px] font-medium uppercase tracking-[0.18em] text-rs-muted">
      Your Crown. Your Style.
    </span>
  </div>
</Link>

            {/* Desktop Nav */}
            <nav className="hidden md:flex items-center gap-8" aria-label="Main navigation">
              {navLinks.map((link) => (
                <NavLink
                  key={link.to}
                  to={link.to}
                  end={link.to === '/'}
                  className={({ isActive }) =>
                    `text-sm tracking-wide transition-colors duration-200 ${
                      isActive ? 'text-rs-ink font-medium' : 'text-rs-muted hover:text-rs-ink'
                    }`
                  }
                >
                  {link.label}
                </NavLink>
              ))}
            </nav>

            {/* Desktop Actions */}
            <div className="hidden md:flex items-center gap-4">
              {/* Search */}
              {searchOpen ? (
                <form onSubmit={handleSearch} className="flex items-center gap-2">
                  <input
                    autoFocus
                    type="text"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    placeholder="Search wigs…"
                    className="text-sm border-b border-rs-ink bg-transparent focus:outline-none px-1 py-1 w-40"
                  />
                  <button type="button" onClick={() => setSearchOpen(false)} className="text-rs-muted hover:text-rs-ink" aria-label="Close search">
                    <X size={16} />
                  </button>
                </form>
              ) : (
                <button
                  onClick={() => setSearchOpen(true)}
                  className="text-rs-muted hover:text-rs-ink transition-colors"
                  aria-label="Open search"
                >
                  <Search size={18} />
                </button>
              )}

              {/* Wishlist */}
              <Link to="/shop" className="relative text-rs-muted hover:text-rs-ink transition-colors" aria-label={`Wishlist (${wishlistCount})`}>
                <Heart size={18} />
                {wishlistCount > 0 && (
                  <span className="absolute -top-1.5 -right-1.5 bg-rs-accent text-white text-[9px] font-bold w-4 h-4 rounded-full flex items-center justify-center">
                    {wishlistCount}
                  </span>
                )}
              </Link>

              {/* Cart */}
              <Link to="/cart" className="relative text-rs-muted hover:text-rs-ink transition-colors" aria-label={`Cart (${itemCount} items)`}>
                <ShoppingBag size={18} />
                {itemCount > 0 && (
                  <span className="absolute -top-1.5 -right-1.5 bg-rs-ink text-white text-[9px] font-bold w-4 h-4 rounded-full flex items-center justify-center">
                    {itemCount}
                  </span>
                )}
              </Link>
            </div>

            {/* Mobile Actions */}
            <div className="md:hidden flex items-center gap-3">
              <Link to="/cart" className="relative text-rs-muted" aria-label="Cart">
                <ShoppingBag size={20} />
                {itemCount > 0 && (
                  <span className="absolute -top-1.5 -right-1.5 bg-rs-ink text-white text-[9px] font-bold w-4 h-4 rounded-full flex items-center justify-center">
                    {itemCount}
                  </span>
                )}
              </Link>
              <button
                onClick={() => setMobileOpen(!mobileOpen)}
                aria-label="Toggle menu"
                className="text-rs-ink"
              >
                {mobileOpen ? <X size={22} /> : <Menu size={22} />}
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Mobile Menu Overlay */}
      {mobileOpen && (
        <div className="fixed inset-0 z-30 md:hidden">
          <div className="absolute inset-0 bg-rs-ink/40" onClick={() => setMobileOpen(false)} />
          <nav
            className="absolute top-16 left-0 right-0 bg-white border-b border-rs-border py-4"
            aria-label="Mobile navigation"
          >
            {navLinks.map((link) => (
              <NavLink
                key={link.to}
                to={link.to}
                end={link.to === '/'}
                onClick={() => setMobileOpen(false)}
                className={({ isActive }) =>
                  `block px-6 py-3 text-sm font-medium ${isActive ? 'text-rs-accent' : 'text-rs-ink'}`
                }
              >
                {link.label}
              </NavLink>
            ))}
            {/* Mobile search */}
            <form onSubmit={handleSearch} className="flex items-center gap-2 px-6 pt-3 border-t border-rs-border mt-2">
              <Search size={16} className="text-rs-muted" />
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Search wigs…"
                className="flex-1 text-sm bg-transparent focus:outline-none py-2"
              />
            </form>
          </nav>
        </div>
      )}

      {/* Spacer */}
      <div className="h-16" />
    </>
  );
}
