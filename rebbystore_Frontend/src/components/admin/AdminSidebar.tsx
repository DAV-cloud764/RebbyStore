import { useState } from 'react';
import { NavLink, Link } from 'react-router-dom';
import {
  LayoutDashboard, Package, Layers, BarChart3, ShoppingCart,
  Users, Settings, ChevronDown, ChevronRight, ArrowRightLeft, X, Menu
} from 'lucide-react';
import { storeConfig } from '../../config/store';

interface NavItem {
  label: string;
  to?: string;
  icon: React.ReactNode;
  children?: { label: string; to: string }[];
}

const navItems: NavItem[] = [
  { label: 'Dashboard', to: '/admin', icon: <LayoutDashboard size={16} /> },
  {
    label: 'Catalog',
    icon: <Package size={16} />,
    children: [
      { label: 'Products', to: '/admin/products' },
    ],
  },
  {
    label: 'Inventory',
    icon: <Layers size={16} />,
    children: [
      { label: 'Stock Overview', to: '/admin/inventory' },
      { label: 'Stock Movements', to: '/admin/inventory/movements' },
    ],
  },
  { label: 'Purchases', to: '/admin/purchases', icon: <BarChart3 size={16} /> },
  {
    label: 'Sales',
    icon: <ShoppingCart size={16} />,
    children: [
      { label: 'Orders', to: '/admin/orders' },
    ],
  },
  { label: 'Customers', to: '/admin/customers', icon: <Users size={16} /> },
  { label: 'Settings', to: '/admin/settings', icon: <Settings size={16} /> },
];

function NavGroup({ item }: { item: NavItem }) {
  const [open, setOpen] = useState(true);
  const isGroup = !!item.children;

  if (!isGroup && item.to) {
    return (
      <NavLink
        to={item.to}
        end={item.to === '/admin'}
        className={({ isActive }) =>
          `flex items-center gap-3 px-4 py-2.5 text-sm transition-colors rounded-sm ${
            isActive
              ? 'bg-rs-accent/10 text-rs-accent font-medium'
              : 'text-white/70 hover:text-white hover:bg-white/5'
          }`
        }
      >
        <span className="shrink-0">{item.icon}</span>
        {item.label}
      </NavLink>
    );
  }

  return (
    <div>
      <button
        onClick={() => setOpen(!open)}
        className="w-full flex items-center justify-between gap-3 px-4 py-2.5 text-sm text-white/70 hover:text-white hover:bg-white/5 transition-colors"
      >
        <span className="flex items-center gap-3">
          <span className="shrink-0">{item.icon}</span>
          {item.label}
        </span>
        {open ? <ChevronDown size={13} /> : <ChevronRight size={13} />}
      </button>
      {open && item.children && (
        <div className="ml-9 border-l border-white/10 pl-3 mt-0.5 space-y-0.5">
          {item.children.map((child) => (
            <NavLink
              key={child.to}
              to={child.to}
              className={({ isActive }) =>
                `block py-2 px-2 text-xs transition-colors rounded-sm ${
                  isActive ? 'text-rs-accent font-medium' : 'text-white/60 hover:text-white'
                }`
              }
            >
              {child.label}
            </NavLink>
          ))}
        </div>
      )}
    </div>
  );
}

interface AdminSidebarProps {
  mobileOpen: boolean;
  onMobileClose: () => void;
}

export function AdminSidebar({ mobileOpen, onMobileClose }: AdminSidebarProps) {
  const sidebar = (
    <div className="w-56 bg-rs-ink h-full flex flex-col">
      {/* Logo */}
      <div className="px-5 py-5 border-b border-white/10">
        <Link to="/" className="font-display text-lg font-bold text-white tracking-[0.08em] hover:text-rs-accent transition-colors">
          {storeConfig.name}
        </Link>
        <p className="text-[10px] text-white/40 tracking-wider uppercase mt-0.5">Admin Panel</p>
      </div>

      {/* Nav */}
      <nav className="flex-1 overflow-y-auto py-4 space-y-0.5 px-2" aria-label="Admin navigation">
        {navItems.map((item) => (
          <NavGroup key={item.label} item={item} />
        ))}
      </nav>

      {/* Footer */}
      <div className="px-5 py-4 border-t border-white/10">
        <Link to="/" className="flex items-center gap-2 text-xs text-white/50 hover:text-white transition-colors">
          <ArrowRightLeft size={13} />
          View Store
        </Link>
      </div>
    </div>
  );

  return (
    <>
      {/* Desktop sidebar */}
      <div className="hidden lg:flex shrink-0 h-screen sticky top-0">
        {sidebar}
      </div>

      {/* Mobile overlay */}
      {mobileOpen && (
        <div className="fixed inset-0 z-50 lg:hidden flex">
          <div className="absolute inset-0 bg-rs-ink/50" onClick={onMobileClose} />
          <div className="relative flex h-full">
            {sidebar}
            <button
              onClick={onMobileClose}
              className="absolute top-4 right-4 text-white/60 hover:text-white"
              aria-label="Close menu"
            >
              <X size={20} />
            </button>
          </div>
        </div>
      )}
    </>
  );
}

interface AdminHeaderProps {
  title: string;
  onMenuClick: () => void;
  actions?: React.ReactNode;
}

export function AdminHeader({ title, onMenuClick, actions }: AdminHeaderProps) {
  return (
    <header className="bg-white border-b border-rs-border px-4 md:px-6 py-4 flex items-center justify-between gap-4 sticky top-0 z-30">
      <div className="flex items-center gap-3">
        <button
          onClick={onMenuClick}
          className="lg:hidden text-rs-muted hover:text-rs-ink"
          aria-label="Open menu"
        >
          <Menu size={20} />
        </button>
        <h1 className="font-display font-semibold text-rs-ink">{title}</h1>
      </div>
      {actions && <div className="flex items-center gap-2">{actions}</div>}
    </header>
  );
}
