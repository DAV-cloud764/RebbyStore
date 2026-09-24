import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { ChevronLeft } from 'lucide-react';
import { useCart } from '../../contexts/CartContext';
import { useToast } from '../../contexts/ToastContext';
import {
  CustomerInformation,
  DeliveryInformation,
  CheckoutOrderSummary,
} from '../../components/checkout/CheckoutComponents';
import type {
  CustomerFormData,
  DeliveryFormData,
} from '../../components/checkout/CheckoutComponents';
import { Button } from '../../components/ui/Button';
import { orderService } from '../../services/orderService';

interface FormErrors {
  customer: Partial<Record<keyof CustomerFormData, string>>;
  delivery: Partial<Record<keyof DeliveryFormData, string>>;
}

const INITIAL_CUSTOMER: CustomerFormData = { fullName: '', phone: '', email: '' };
const INITIAL_DELIVERY: DeliveryFormData = { address: '', city: '', region: '', notes: '' };

function validateEmail(v: string) { return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v); }
function validatePhone(v: string) { return /^0[67]\d{8}$/.test(v.replace(/\s/g, '')); }

export default function Checkout() {
  const { items, total, clearCart } = useCart();
  const { showToast } = useToast();
  const navigate = useNavigate();

  const [customer, setCustomer] = useState<CustomerFormData>(INITIAL_CUSTOMER);
  const [delivery, setDelivery] = useState<DeliveryFormData>(INITIAL_DELIVERY);
  const [errors, setErrors] = useState<FormErrors>({ customer: {}, delivery: {} });
  const [submitting, setSubmitting] = useState(false);

  if (items.length === 0) {
    return (
      <div className="page-container py-20 text-center">
        <h2 className="font-display text-2xl font-bold text-rs-ink mb-3">Your cart is empty</h2>
        <Link to="/shop" className="btn-primary inline-flex mt-2">Go Shopping</Link>
      </div>
    );
  }

  function handleCustomerChange(field: keyof CustomerFormData, value: string) {
    setCustomer((p) => ({ ...p, [field]: value }));
    if (errors.customer[field]) {
      setErrors((e) => ({ ...e, customer: { ...e.customer, [field]: undefined } }));
    }
  }

  function handleDeliveryChange(field: keyof DeliveryFormData, value: string) {
    setDelivery((p) => ({ ...p, [field]: value }));
    if (errors.delivery[field as keyof typeof errors.delivery]) {
      setErrors((e) => ({ ...e, delivery: { ...e.delivery, [field]: undefined } }));
    }
  }

  function validate(): boolean {
    const ce: FormErrors['customer'] = {};
    const de: FormErrors['delivery'] = {};
    let valid = true;

    if (!customer.fullName.trim()) { ce.fullName = 'Full name is required'; valid = false; }
    if (!customer.phone.trim()) { ce.phone = 'Phone number is required'; valid = false; }
    else if (!validatePhone(customer.phone)) { ce.phone = 'Enter a valid Tanzanian phone number (e.g. 0712345678)'; valid = false; }
    if (!customer.email.trim()) { ce.email = 'Email address is required'; valid = false; }
    else if (!validateEmail(customer.email)) { ce.email = 'Enter a valid email address'; valid = false; }

    if (!delivery.address.trim()) { de.address = 'Delivery address is required'; valid = false; }
    if (!delivery.city.trim()) { de.city = 'City is required'; valid = false; }
    if (!delivery.region) { de.region = 'Please select your region'; valid = false; }

    setErrors({ customer: ce, delivery: de });
    return valid;
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!validate()) {
      showToast('Please fill in all required fields', 'error');
      return;
    }

    setSubmitting(true);
    try {
      const order = await orderService.create({
  customer,
  delivery,
  items: items.map((item) => ({
    product: {
      id: item.product.id,
    },
    quantity: item.quantity,
  })),
  paymentMethod: 'cash-on-delivery',
});
      clearCart();
      navigate(`/order-confirmation/${order.id}`);
    } catch {
      showToast('Something went wrong. Please try again.', 'error');
      setSubmitting(false);
    }
  }

  return (
    <div className="page-container py-8">
      {/* Breadcrumb */}
      <nav className="flex items-center gap-2 mb-8 text-sm text-rs-muted">
        <Link to="/cart" className="flex items-center gap-1 hover:text-rs-ink transition-colors">
          <ChevronLeft size={14} /> Cart
        </Link>
        <span>/</span>
        <span className="text-rs-ink font-medium">Checkout</span>
      </nav>

      <div className="mb-8">
        <span className="section-label block mb-1">Almost There</span>
        <h1 className="font-display text-4xl font-bold text-rs-ink">Checkout</h1>
      </div>

      <form onSubmit={handleSubmit} noValidate>
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 lg:gap-12">
          {/* Form sections */}
          <div className="lg:col-span-2 flex flex-col gap-10">
            <CustomerInformation data={customer} errors={errors.customer} onChange={handleCustomerChange} />

            <div className="border-t border-rs-border pt-8">
              <DeliveryInformation data={delivery} errors={errors.delivery} onChange={handleDeliveryChange} />
            </div>

            {/* Payment — display only */}
            <div className="border-t border-rs-border pt-8">
              <h2 className="font-display font-semibold text-rs-ink text-xl mb-5">Payment Method</h2>
              <div className="border border-rs-ink p-5 flex items-start gap-4">
                <div className="w-5 h-5 border-2 border-rs-ink rounded-full mt-0.5 flex items-center justify-center shrink-0">
                  <div className="w-2.5 h-2.5 rounded-full bg-rs-ink" />
                </div>
                <div>
                  <p className="font-semibold text-rs-ink">Cash on Delivery</p>
                  <p className="text-sm text-rs-muted mt-1">
                    Pay when your order arrives. Have the exact amount ready for the delivery agent.
                  </p>
                </div>
              </div>
            </div>

            {/* Submit */}
            <div className="border-t border-rs-border pt-6">
              <Button
                type="submit"
                variant="primary"
                size="lg"
                loading={submitting}
                className="w-full justify-center sm:w-auto"
              >
                Place Order
              </Button>
              <p className="text-xs text-rs-muted mt-3">
                By placing your order, you agree to pay on delivery. No online payment is required.
              </p>
            </div>
          </div>

          {/* Order summary sidebar */}
          <div className="lg:col-span-1">
            <CheckoutOrderSummary items={items} subtotal={total} />
          </div>
        </div>
      </form>
    </div>
  );
}
