
type BadgeVariant = 'new' | 'low-stock' | 'out-of-stock' | 'sale' | 'in-stock' | 'pending' | 'confirmed' | 'processing' | 'ready' | 'delivered' | 'cancelled' | 'neutral';

const variantClasses: Record<BadgeVariant, string> = {
  'new': 'bg-rs-ink text-white',
  'low-stock': 'bg-amber-50 text-amber-700 border border-amber-200',
  'out-of-stock': 'bg-red-50 text-red-600 border border-red-200',
  'sale': 'bg-rs-accent text-white',
  'in-stock': 'bg-green-50 text-green-700 border border-green-200',
  'pending': 'bg-purple-50 text-purple-700 border border-purple-200',
  'confirmed': 'bg-blue-50 text-blue-700 border border-blue-200',
  'processing': 'bg-amber-50 text-amber-700 border border-amber-200',
  'ready': 'bg-cyan-50 text-cyan-700 border border-cyan-200',
  'delivered': 'bg-green-50 text-green-700 border border-green-200',
  'cancelled': 'bg-red-50 text-red-600 border border-red-200',
  'neutral': 'bg-rs-surface text-rs-muted border border-rs-border',
};

interface BadgeProps {
  variant?: BadgeVariant;
  children: React.ReactNode;
  className?: string;
}

export function Badge({ variant = 'neutral', children, className = '' }: BadgeProps) {
  return (
    <span className={`inline-flex items-center px-2 py-0.5 text-[10px] font-semibold tracking-wider uppercase ${variantClasses[variant]} ${className}`}>
      {children}
    </span>
  );
}

export function orderStatusBadgeVariant(status: string): BadgeVariant {
  const map: Record<string, BadgeVariant> = {
    pending: 'pending',
    confirmed: 'confirmed',
    processing: 'processing',
    'ready-for-delivery': 'ready',
    delivered: 'delivered',
    cancelled: 'cancelled',
  };
  return map[status] ?? 'neutral';
}

export function orderStatusLabel(status: string): string {
  const map: Record<string, string> = {
    pending: 'Pending',
    confirmed: 'Confirmed',
    processing: 'Processing',
    'ready-for-delivery': 'Ready for Delivery',
    delivered: 'Delivered',
    cancelled: 'Cancelled',
  };
  return map[status] ?? status;
}
