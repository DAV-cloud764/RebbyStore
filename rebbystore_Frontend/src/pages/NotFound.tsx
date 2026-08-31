import { Link } from 'react-router-dom';

export default function NotFound() {
  return (
    <div className="min-h-[70vh] flex flex-col items-center justify-center text-center px-6">
      <p className="text-8xl font-display font-bold text-rs-border">404</p>
      <h1 className="font-display text-3xl font-bold text-rs-ink mt-4 mb-2">Page Not Found</h1>
      <p className="text-rs-muted mb-8 max-w-sm">The page you're looking for doesn't exist or has been moved.</p>
      <div className="flex gap-4">
        <Link to="/" className="btn-primary">Go Home</Link>
        <Link to="/shop" className="btn-secondary">Shop Now</Link>
      </div>
    </div>
  );
}
