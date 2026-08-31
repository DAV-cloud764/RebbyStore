import { useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { Save } from 'lucide-react';
import { AdminHeader } from '../../components/admin/AdminSidebar';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { useToast } from '../../contexts/ToastContext';
import { storeConfig } from '../../config/store';

interface OutletCtx { onMenuClick: () => void; }

export default function Settings() {
  const { onMenuClick } = useOutletContext<OutletCtx>();
  const { showToast } = useToast();
  const [saving, setSaving] = useState(false);

  const [form, setForm] = useState({
    storeName: storeConfig.name,
    phone: storeConfig.phone,
    email: storeConfig.email,
    location: storeConfig.location,
    deliveryFee: String(storeConfig.deliveryFee),
    freeDeliveryThreshold: String(storeConfig.freeDeliveryThreshold),
    instagram: storeConfig.social.instagram,
    whatsapp: storeConfig.social.whatsapp,
    tiktok: storeConfig.social.tiktok,
  });

  function handleChange(k: string, v: string) { setForm((p) => ({ ...p, [k]: v })); }

  async function handleSave(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    await new Promise((r) => setTimeout(r, 600));
    setSaving(false);
    showToast('Settings saved (in-memory). Connect a backend to persist changes.');
  }

  return (
    <>
      <AdminHeader title="Settings" onMenuClick={onMenuClick} />
      <main className="p-4 md:p-6">
        <form onSubmit={handleSave} className="max-w-xl space-y-6">
          {/* Store info */}
          <div className="bg-white border border-rs-border p-5 space-y-4">
            <h2 className="font-display font-semibold text-rs-ink">Store Information</h2>
            <Input label="Store Name" value={form.storeName} onChange={(e) => handleChange('storeName', e.target.value)} />
            <Input label="Phone Number" value={form.phone} onChange={(e) => handleChange('phone', e.target.value)} />
            <Input label="Email Address" type="email" value={form.email} onChange={(e) => handleChange('email', e.target.value)} />
            <Input label="Location" value={form.location} onChange={(e) => handleChange('location', e.target.value)} />
          </div>

          {/* Delivery */}
          <div className="bg-white border border-rs-border p-5 space-y-4">
            <h2 className="font-display font-semibold text-rs-ink">Delivery Settings</h2>
            <div className="grid grid-cols-2 gap-4">
              <Input label="Delivery Fee (TSh)" type="number" value={form.deliveryFee} onChange={(e) => handleChange('deliveryFee', e.target.value)} />
              <Input label="Free Delivery Threshold (TSh)" type="number" value={form.freeDeliveryThreshold} onChange={(e) => handleChange('freeDeliveryThreshold', e.target.value)} />
            </div>
            <p className="text-xs text-rs-muted">Orders above the threshold qualify for free delivery.</p>
          </div>

          {/* Social */}
          <div className="bg-white border border-rs-border p-5 space-y-4">
            <h2 className="font-display font-semibold text-rs-ink">Social Media</h2>
            <Input label="Instagram URL" value={form.instagram} onChange={(e) => handleChange('instagram', e.target.value)} />
            <Input label="WhatsApp Link" value={form.whatsapp} onChange={(e) => handleChange('whatsapp', e.target.value)} />
            <Input label="TikTok URL" value={form.tiktok} onChange={(e) => handleChange('tiktok', e.target.value)} />
          </div>

          <div className="p-4 bg-rs-surface border border-rs-border text-xs text-rs-muted">
            <strong className="text-rs-ink">Note:</strong> Settings are stored in-memory in this prototype. To persist changes, integrate this form with a backend API or a config file. The store config source of truth is <code className="font-mono bg-white px-1">src/config/store.ts</code>.
          </div>

          <Button type="submit" loading={saving} size="lg">
            <Save size={15} /> Save Settings
          </Button>
        </form>
      </main>
    </>
  );
}
