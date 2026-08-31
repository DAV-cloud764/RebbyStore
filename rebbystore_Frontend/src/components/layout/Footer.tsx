import { Link } from 'react-router-dom';
import { MapPin, Phone, Mail, ExternalLink, MessageCircle } from 'lucide-react';
import { storeConfig } from '../../config/store';
import  BrandLogo  from "../ui/BrandLogo";

// TikTok icon (simple SVG since Lucide doesn't include it)
function TikTokIcon({ size = 18 }: { size?: number }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
      <path d="M19.59 6.69a4.83 4.83 0 01-3.77-4.25V2h-3.45v13.67a2.89 2.89 0 01-2.88 2.5 2.89 2.89 0 01-2.89-2.89 2.89 2.89 0 012.89-2.89c.28 0 .54.04.79.1V9.01a6.33 6.33 0 00-.79-.05 6.34 6.34 0 00-6.34 6.34 6.34 6.34 0 006.34 6.34 6.34 6.34 0 006.33-6.34V8.69a8.18 8.18 0 004.78 1.52V6.76a4.85 4.85 0 01-1.01-.07z"/>
    </svg>
  );
}

export function Footer() {
  return (
    <footer className="bg-rs-ink text-white">
      <div className="page-container py-14">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-10">
          {/* Brand */}
          <div>
            <BrandLogo className="h-16 w-auto mb-4" />
            <p className="text-sm text-white/60 mb-6 max-w-xs leading-relaxed">
              Premium wigs for the modern woman. Quality you can feel, style you can own.
            </p>

            {/* Contact info */}
            <ul className="space-y-2.5">
              <li className="flex items-start gap-2.5 text-sm text-white/70">
                <MapPin size={15} className="mt-0.5 shrink-0 text-rs-accent" />
                {storeConfig.location}
              </li>
              <li>
                <a href={`tel:${storeConfig.phone}`} className="flex items-center gap-2.5 text-sm text-white/70 hover:text-white transition-colors">
                  <Phone size={15} className="text-rs-accent" />
                  {storeConfig.phone}
                </a>
              </li>
              <li>
                <a href={`mailto:${storeConfig.email}`} className="flex items-center gap-2.5 text-sm text-white/70 hover:text-white transition-colors">
                  <Mail size={15} className="text-rs-accent" />
                  {storeConfig.email}
                </a>
              </li>
            </ul>
          </div>

          {/* Navigation */}
          <div>
            <h3 className="section-label text-white/40 mb-5">Navigation</h3>
            <ul className="space-y-3">
              {[
                { to: '/', label: 'Home' },
                { to: '/shop', label: 'Shop All' },
                { to: '/shop?category=human-hair', label: 'Human Hair' },
                { to: '/shop?category=lace-front', label: 'Lace Front' },
                { to: '/shop?category=bob', label: 'Bob Wigs' },
                { to: '/cart', label: 'My Cart' },
              ].map((link) => (
                <li key={link.to}>
                  <Link to={link.to} className="text-sm text-white/60 hover:text-white transition-colors">
                    {link.label}
                  </Link>
                </li>
              ))}
            </ul>
          </div>

          {/* Social & Info */}
          <div>
            <h3 className="section-label text-white/40 mb-5">Follow Us</h3>
            <div className="flex gap-3 mb-8">
              <a
                href={storeConfig.social.instagram}
                target="_blank"
                rel="noopener noreferrer"
                aria-label="Instagram"
                className="w-9 h-9 flex items-center justify-center border border-white/20 text-white/60 hover:text-white hover:border-white/60 transition-colors"
              >
                <ExternalLink size={16} />
              </a>
              <a
                href={storeConfig.social.whatsapp}
                target="_blank"
                rel="noopener noreferrer"
                aria-label="WhatsApp"
                className="w-9 h-9 flex items-center justify-center border border-white/20 text-white/60 hover:text-white hover:border-white/60 transition-colors"
              >
                <MessageCircle size={16} />
              </a>
              <a
                href={storeConfig.social.tiktok}
                target="_blank"
                rel="noopener noreferrer"
                aria-label="TikTok"
                className="w-9 h-9 flex items-center justify-center border border-white/20 text-white/60 hover:text-white hover:border-white/60 transition-colors"
              >
                <TikTokIcon size={15} />
              </a>
            </div>

            <h3 className="section-label text-white/40 mb-5">Payment</h3>
            <div className="border border-white/20 px-4 py-3 text-sm text-white/70">
              <span className="font-medium text-white">Cash on Delivery</span>
              <p className="text-xs text-white/50 mt-1">Pay when your order arrives.</p>
            </div>
          </div>
        </div>
      </div>

      {/* Bottom bar */}
      <div className="border-t border-white/10">
        <div className="page-container py-4 flex flex-col sm:flex-row items-center justify-between gap-2 text-xs text-white/40">
          <span>© {new Date().getFullYear()} {storeConfig.name}. All rights reserved.</span>
          <span>Dar es Salaam, Tanzania</span>
        </div>
      </div>
    </footer>
  );
}
