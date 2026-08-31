import { Input, Textarea } from '../ui/Input';
import { formatPrice } from '../../utils/formatting';
import type { CartItem } from '../../types/order';
import { storeConfig } from '../../config/store';
import { Truck } from 'lucide-react';

// ──────────────────────────────────────────
// CustomerInformation
// ──────────────────────────────────────────
export interface CustomerFormData {
  fullName: string;
  phone: string;
  email: string;
}

interface CustomerInfoErrors { fullName?: string; phone?: string; email?: string; }

interface CustomerInformationProps {
  data: CustomerFormData;
  errors: CustomerInfoErrors;
  onChange: (field: keyof CustomerFormData, value: string) => void;
}

export function CustomerInformation({ data, errors, onChange }: CustomerInformationProps) {
  return (
    <section>
      <h2 className="font-display font-semibold text-rs-ink text-xl mb-5">Customer Information</h2>
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div className="sm:col-span-2">
          <Input
            label="Full Name"
            value={data.fullName}
            onChange={(e) => onChange('fullName', e.target.value)}
            placeholder="Your full name"
            error={errors.fullName}
            autoComplete="name"
          />
        </div>
        <Input
          label="Phone Number"
          value={data.phone}
          onChange={(e) => onChange('phone', e.target.value)}
          placeholder="07XX XXX XXX"
          error={errors.phone}
          type="tel"
          autoComplete="tel"
        />
        <Input
          label="Email Address"
          value={data.email}
          onChange={(e) => onChange('email', e.target.value)}
          placeholder="your@email.com"
          error={errors.email}
          type="email"
          autoComplete="email"
        />
      </div>
    </section>
  );
}

// ──────────────────────────────────────────
// DeliveryInformation
// ──────────────────────────────────────────
export interface DeliveryFormData {
  address: string;
  city: string;
  region: string;
  notes: string;
}

interface DeliveryInfoErrors { address?: string; city?: string; region?: string; }

interface DeliveryInformationProps {
  data: DeliveryFormData;
  errors: DeliveryInfoErrors;
  onChange: (field: keyof DeliveryFormData, value: string) => void;
}

const tanzaniaRegions = [
  'Dar es Salaam', 'Arusha', 'Mwanza', 'Dodoma', 'Mbeya', 'Morogoro',
  'Tanga', 'Zanzibar', 'Kilimanjaro', 'Pwani', 'Iringa', 'Kagera',
  'Shinyanga', 'Tabora', 'Rukwa', 'Ruvuma', 'Lindi', 'Mtwara',
  'Singida', 'Kigoma', 'Geita', 'Simiyu', 'Manyara', 'Katavi', 'Njombe',
];

export function DeliveryInformation({ data, errors, onChange }: DeliveryInformationProps) {
  return (
    <section>
      <h2 className="font-display font-semibold text-rs-ink text-xl mb-5">Delivery Information</h2>
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div className="sm:col-span-2">
          <Input
            label="Street Address"
            value={data.address}
            onChange={(e) => onChange('address', e.target.value)}
            placeholder="Street / Area / Building"
            error={errors.address}
            autoComplete="street-address"
          />
        </div>
        <Input
          label="City / Town"
          value={data.city}
          onChange={(e) => onChange('city', e.target.value)}
          placeholder="City or town"
          error={errors.city}
        />
        <div>
          <label className="label-base" htmlFor="region">Region</label>
          <select
            id="region"
            value={data.region}
            onChange={(e) => onChange('region', e.target.value)}
            className={`input-base ${errors.region ? 'border-red-400' : ''}`}
          >
            <option value="">Select region…</option>
            {tanzaniaRegions.map((r) => (
              <option key={r} value={r}>{r}</option>
            ))}
          </select>
          {errors.region && <p className="mt-1 text-xs text-red-600">{errors.region}</p>}
        </div>
        <div className="sm:col-span-2">
          <Textarea
            label="Delivery Notes (optional)"
            value={data.notes}
            onChange={(e) => onChange('notes', e.target.value)}
            placeholder="e.g. Call before delivery, gate code, landmark…"
            rows={3}
          />
        </div>
      </div>
    </section>
  );
}

// ──────────────────────────────────────────
// OrderSummary (checkout sidebar)
// ──────────────────────────────────────────
interface CheckoutOrderSummaryProps {
  items: CartItem[];
  subtotal: number;
}

export function CheckoutOrderSummary({ items, subtotal }: CheckoutOrderSummaryProps) {
  const deliveryFee = subtotal >= storeConfig.freeDeliveryThreshold ? 0 : storeConfig.deliveryFee;
  const total = subtotal + deliveryFee;

  return (
    <div className="bg-rs-surface p-6 sticky top-24">
      <h2 className="font-display font-semibold text-rs-ink mb-5">Your Order</h2>

      {/* Items */}
      <div className="space-y-3 mb-5">
        {items.map(({ product, quantity }) => (
          <div key={product.id} className="flex gap-3">
            <img src={product.images[0]} alt={product.name} className="w-12 h-16 object-cover bg-white shrink-0" />
            <div className="flex-1 min-w-0">
              <p className="text-xs font-medium text-rs-ink truncate">{product.name}</p>
              <p className="text-[11px] text-rs-muted">{product.length} · Qty {quantity}</p>
              <p className="text-xs font-semibold text-rs-ink mt-0.5">{formatPrice(product.price * quantity)}</p>
            </div>
          </div>
        ))}
      </div>

      {/* Totals */}
      <div className="border-t border-rs-border pt-4 space-y-2.5 text-sm">
        <div className="flex justify-between">
          <span className="text-rs-muted">Subtotal</span>
          <span className="font-medium">{formatPrice(subtotal)}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-rs-muted flex items-center gap-1"><Truck size={12} /> Delivery</span>
          <span className={`font-medium ${deliveryFee === 0 ? 'text-green-600' : ''}`}>
            {deliveryFee === 0 ? 'Free' : formatPrice(deliveryFee)}
          </span>
        </div>
        <div className="border-t border-rs-border pt-3 flex justify-between font-semibold text-rs-ink">
          <span>Total</span>
          <span>{formatPrice(total)}</span>
        </div>
      </div>

      {/* Payment method */}
      <div className="mt-5 p-3 bg-white border border-rs-border text-xs">
        <p className="section-label mb-1.5">Payment Method</p>
        <div className="flex items-center gap-2">
          <span className="w-2 h-2 rounded-full bg-green-500" />
          <span className="font-medium text-rs-ink">Cash on Delivery</span>
        </div>
        <p className="text-rs-muted mt-1.5">Pay when your order is delivered to your door.</p>
      </div>
    </div>
  );
}
