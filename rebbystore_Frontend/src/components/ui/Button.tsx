
type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'accent' | 'danger' | 'outline-white';
type ButtonSize = 'sm' | 'md' | 'lg';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  size?: ButtonSize;
  loading?: boolean;
  children: React.ReactNode;
}

const variantClasses: Record<ButtonVariant, string> = {
  primary: 'bg-rs-ink text-white hover:bg-rs-accent focus:ring-rs-ink',
  secondary: 'border border-rs-ink text-rs-ink hover:bg-rs-ink hover:text-white focus:ring-rs-ink',
  ghost: 'text-rs-muted hover:text-rs-ink focus:ring-rs-border',
  accent: 'bg-rs-accent text-white hover:bg-rs-gold focus:ring-rs-accent',
  danger: 'bg-red-600 text-white hover:bg-red-700 focus:ring-red-600',
  'outline-white': 'border border-white text-white hover:bg-white hover:text-rs-ink focus:ring-white',
};

const sizeClasses: Record<ButtonSize, string> = {
  sm: 'text-xs px-4 py-2',
  md: 'text-sm px-6 py-3',
  lg: 'text-sm px-8 py-4',
};

export function Button({
  variant = 'primary',
  size = 'md',
  loading = false,
  children,
  className = '',
  disabled,
  ...props
}: ButtonProps) {
  return (
    <button
      {...props}
      disabled={disabled || loading}
      className={[
        'inline-flex items-center justify-center gap-2 font-medium tracking-wide transition-all duration-200',
        'focus:outline-none focus:ring-2 focus:ring-offset-1',
        'disabled:opacity-50 disabled:cursor-not-allowed',
        variantClasses[variant],
        sizeClasses[size],
        className,
      ].join(' ')}
    >
      {loading && (
        <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24" fill="none">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z" />
        </svg>
      )}
      {children}
    </button>
  );
}
