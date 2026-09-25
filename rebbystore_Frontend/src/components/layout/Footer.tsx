import { Link } from 'react-router-dom';
import { LogIn, MapPin, Phone, Mail } from 'lucide-react';
import { FaInstagram, FaWhatsapp, FaTiktok } from 'react-icons/fa';
import { storeConfig } from '../../config/store';
import BrandLogo from '../ui/BrandLogo';

export function Footer() {
  return (
    <footer className="bg-rs-ink text-white">
      <div className="page-container py-14">
        <div className="grid grid-cols-1 gap-10 md:grid-cols-3">
          {/* Brand */}
          <div>
            <BrandLogo className="w-16 h-16 object-cover rounded-full" />

            <p className="mb-6 max-w-xs text-sm leading-relaxed text-white/60">
              Premium wigs for the modern woman. Quality you can feel, style you can own.
            </p>

            {/* Contact info */}
            <ul className="space-y-2.5">
              <li className="flex items-start gap-2.5 text-sm text-white/70">
                <MapPin
                  size={15}
                  className="mt-0.5 shrink-0 text-rs-accent"
                />
                <span>{storeConfig.location}</span>
              </li>

              <li>
                <a
                  href={`tel:${storeConfig.phone}`}
                  className="flex items-center gap-2.5 text-sm text-white/70 transition-colors hover:text-white"
                >
                  <Phone size={15} className="shrink-0 text-rs-accent" />
                  <span>{storeConfig.phone}</span>
                </a>
              </li>

              <li>
                <a
                  href={`mailto:${storeConfig.email}`}
                  className="flex items-center gap-2.5 text-sm text-white/70 transition-colors hover:text-white"
                >
                  <Mail size={15} className="shrink-0 text-rs-accent" />
                  <span>{storeConfig.email}</span>
                </a>
              </li>
            </ul>
          </div>

          {/* Navigation */}
          <div>
            <h3 className="section-label mb-5 text-white/40">
              Navigation
            </h3>

            <ul className="space-y-3">
  {[
    { to: '/', label: 'Home' },
    { to: '/shop', label: 'Shop All' },
    {
      to: '/shop?category=human-hair',
      label: 'Human Hair',
    },
    {
      to: '/shop?category=lace-front',
      label: 'Lace Front',
    },
    {
      to: '/shop?category=bob',
      label: 'Bob Wigs',
    },
    { to: '/cart', label: 'My Cart' },
  ].map((link) => (
    <li key={link.to}>
      <Link
        to={link.to}
        className="text-sm text-white/60 transition-colors hover:text-white"
      >
        {link.label}
      </Link>
    </li>
  ))}
</ul>

          </div>

          {/* Social & Info */}
          <div>
            <h3 className="section-label mb-5 text-white/40">
              Follow Us
            </h3>

            {/* Social & Info */}
<div>

  {/* Social icons */}
  <div className="mb-8 flex gap-3">
    {/* Instagram */}
    <a
      href={storeConfig.social.instagram}
      target="_blank"
      rel="noopener noreferrer"
      aria-label="Instagram"
      className="group flex h-10 w-10 items-center justify-center rounded-full border border-white/20 text-white/60 transition-all duration-200 hover:-translate-y-0.5 hover:border-white/50 hover:bg-white/10 hover:text-white"
    >
      <FaInstagram
        size={17}
        className="transition-transform duration-200 group-hover:scale-110"
      />
    </a>

    {/* WhatsApp */}
    <a
      href={storeConfig.social.whatsapp}
      target="_blank"
      rel="noopener noreferrer"
      aria-label="WhatsApp"
      className="group flex h-10 w-10 items-center justify-center rounded-full border border-white/20 text-white/60 transition-all duration-200 hover:-translate-y-0.5 hover:border-white/50 hover:bg-white/10 hover:text-white"
    >
      <FaWhatsapp
        size={18}
        className="transition-transform duration-200 group-hover:scale-110"
      />
    </a>

    {/* TikTok */}
    <a
      href={storeConfig.social.tiktok}
      target="_blank"
      rel="noopener noreferrer"
      aria-label="TikTok"
      className="group flex h-10 w-10 items-center justify-center rounded-full border border-white/20 text-white/60 transition-all duration-200 hover:-translate-y-0.5 hover:border-white/50 hover:bg-white/10 hover:text-white"
    >
      <FaTiktok
        size={16}
        className="transition-transform duration-200 group-hover:scale-110"
      />
    </a>
  </div>

  {/* Staff Portal */}
  <h3 className="section-label mb-4 text-white/40">
    Staff Portal
  </h3>

  <Link
    to="/login"
    className="mb-8 inline-flex items-center gap-2 text-sm text-white/60 transition-colors hover:text-white"
  >
    <LogIn size={15} />
    Staff Login
  </Link>

  {/* Payment */}
  <h3 className="section-label mb-5 text-white/40">
    Payment
  </h3>

  <div className="border border-white/20 px-4 py-3 text-sm text-white/70">
    <span className="font-medium text-white">
      Cash on Delivery
    </span>

    <p className="mt-1 text-xs text-white/50">
      Pay when your order arrives.
    </p>
  </div>
</div>

      
          </div>
        </div>
      </div>

      {/* Bottom bar */}
      <div className="border-t border-white/10">
        <div className="page-container flex flex-col items-center justify-between gap-2 py-4 text-xs text-white/40 sm:flex-row">
          <span>
            © {new Date().getFullYear()} {storeConfig.name}. All rights reserved.
          </span>

          <span>Dar es Salaam, Tanzania</span>
        </div>
      </div>
    </footer>
  );
}