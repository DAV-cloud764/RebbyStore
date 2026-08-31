import { useState, useEffect } from 'react';
import { useOutletContext, useNavigate, useParams } from 'react-router-dom';
import { AdminHeader } from '../../components/admin/AdminSidebar';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Select } from '../../components/ui/Select';
import { productService } from '../../services/productService';
import type { Product } from '../../types/product';
import { useToast } from '../../contexts/ToastContext';
import { storeConfig } from '../../config/store';

interface OutletCtx { onMenuClick: () => void; }

interface ProductFormData {
  name: string;
  sku: string;
  price: string;
  description: string;
  category: string;
  color: string;
  texture: string;
  length: string;
  hairType: string;
  stockQuantity: string;
  lowStockThreshold: string;
  status: 'active' | 'inactive';
  images: string;
}

const EMPTY_FORM: ProductFormData = {
  name: '', sku: '', price: '', description: '', category: '',
  color: '', texture: '', length: '', hairType: '',
  stockQuantity: '', lowStockThreshold: '3',
  status: 'active', images: '',
};

function productToForm(p: Product): ProductFormData {
  return {
    name: p.name, sku: p.sku, price: String(p.price), description: p.description,
    category: p.category, color: p.color, texture: p.texture, length: p.length,
    hairType: p.hairType, stockQuantity: String(p.stockQuantity),
    lowStockThreshold: String(p.lowStockThreshold), status: p.status,
    images: p.images.join('\n'),
  };
}

function ProductForm({ initialData, onSave, title, onMenuClick }: {
  initialData: ProductFormData;
  onSave: (data: ProductFormData) => Promise<void>;
  title: string;
  onMenuClick: () => void;
}) {
  const [form, setForm] = useState<ProductFormData>(initialData);
  const [saving, setSaving] = useState(false);
  const navigate = useNavigate();

  function handleChange(key: keyof ProductFormData, value: string) {
    setForm((p) => ({ ...p, [key]: value }));
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    try { await onSave(form); } finally { setSaving(false); }
  }

  const categoryOptions = storeConfig.categories.map((c) => ({ value: c.id, label: c.label }));
  const textureOptions = storeConfig.textures.map((t) => ({ value: t.id, label: t.label }));
  const lengthOptions = storeConfig.lengths.map((l) => ({ value: l, label: l }));
  const hairTypeOptions = storeConfig.hairTypes.map((h) => ({ value: h.id, label: h.label }));

  return (
    <>
      <AdminHeader title={title} onMenuClick={onMenuClick} actions={
        <Button variant="ghost" size="sm" onClick={() => navigate('/admin/products')}>Cancel</Button>
      } />
      <main className="p-4 md:p-6">
        <form onSubmit={handleSubmit}>
          <div className="max-w-2xl space-y-6">
            {/* Basic info */}
            <div className="bg-white border border-rs-border p-5 space-y-4">
              <h2 className="font-display font-semibold text-rs-ink">Basic Information</h2>
              <Input label="Product Name" value={form.name} onChange={(e) => handleChange('name', e.target.value)} required placeholder="e.g. Body Wave 20&quot;" />
              <div className="grid grid-cols-2 gap-4">
                <Input label="SKU" value={form.sku} onChange={(e) => handleChange('sku', e.target.value)} required placeholder="BW20-BLK" />
                <Input label="Price (TSh)" type="number" value={form.price} onChange={(e) => handleChange('price', e.target.value)} required placeholder="240000" />
              </div>
              <div>
                <label className="label-base">Description</label>
                <textarea
                  value={form.description}
                  onChange={(e) => handleChange('description', e.target.value)}
                  rows={4}
                  className="input-base resize-none"
                  placeholder="Describe the wig — texture, quality, style…"
                />
              </div>
            </div>

            {/* Attributes */}
            <div className="bg-white border border-rs-border p-5 space-y-4">
              <h2 className="font-display font-semibold text-rs-ink">Attributes</h2>
              <div className="grid grid-cols-2 gap-4">
                <Select label="Category" value={form.category} onChange={(e) => handleChange('category', e.target.value)} options={categoryOptions} placeholder="Select category" required />
                <Select label="Hair Type" value={form.hairType} onChange={(e) => handleChange('hairType', e.target.value)} options={hairTypeOptions} placeholder="Select hair type" required />
                <Select label="Texture" value={form.texture} onChange={(e) => handleChange('texture', e.target.value)} options={textureOptions} placeholder="Select texture" required />
                <Select label="Length" value={form.length} onChange={(e) => handleChange('length', e.target.value)} options={lengthOptions} placeholder="Select length" required />
                <Input label="Colour" value={form.color} onChange={(e) => handleChange('color', e.target.value)} placeholder="e.g. Natural Black" required />
              </div>
            </div>

            {/* Inventory */}
            <div className="bg-white border border-rs-border p-5 space-y-4">
              <h2 className="font-display font-semibold text-rs-ink">Inventory</h2>
              <div className="grid grid-cols-2 gap-4">
                <Input label="Stock Quantity" type="number" value={form.stockQuantity} onChange={(e) => handleChange('stockQuantity', e.target.value)} required min={0} />
                <Input label="Low Stock Threshold" type="number" value={form.lowStockThreshold} onChange={(e) => handleChange('lowStockThreshold', e.target.value)} required min={1} />
              </div>
              <div>
                <label className="label-base">Status</label>
                <div className="flex gap-6 mt-1">
                  {(['active', 'inactive'] as const).map((s) => (
                    <label key={s} className="flex items-center gap-2 cursor-pointer text-sm text-rs-ink capitalize">
                      <input type="radio" name="status" value={s} checked={form.status === s} onChange={() => handleChange('status', s)} className="accent-rs-ink" />
                      {s}
                    </label>
                  ))}
                </div>
              </div>
            </div>

            {/* Images */}
            <div className="bg-white border border-rs-border p-5 space-y-3">
              <h2 className="font-display font-semibold text-rs-ink">Images</h2>
              <p className="text-xs text-rs-muted">Enter image URLs, one per line. Replace with real product photo URLs when available.</p>
              <textarea
                value={form.images}
                onChange={(e) => handleChange('images', e.target.value)}
                rows={4}
                className="input-base resize-none font-mono text-xs"
                placeholder="https://example.com/image1.jpg&#10;https://example.com/image2.jpg"
              />
            </div>

            <div className="flex gap-3">
              <Button type="submit" loading={saving}>
                {saving ? 'Saving…' : 'Save Product'}
              </Button>
              <Button variant="secondary" type="button" onClick={() => navigate('/admin/products')}>
                Cancel
              </Button>
            </div>
          </div>
        </form>
      </main>
    </>
  );
}

// ─── AddProduct ───────────────────────────────────────────────────────────────
export function AddProduct() {
  const { onMenuClick } = useOutletContext<OutletCtx>();
  const navigate = useNavigate();
  const { showToast } = useToast();

  async function handleSave(data: ProductFormData) {
    await productService.create({
      name: data.name, sku: data.sku, price: Number(data.price),
      description: data.description, category: data.category as Product['category'],
      color: data.color, texture: data.texture as Product['texture'],
      length: data.length, hairType: data.hairType as Product['hairType'],
      stockQuantity: Number(data.stockQuantity),
      lowStockThreshold: Number(data.lowStockThreshold),
      status: data.status,
      images: data.images.split('\n').map((s) => s.trim()).filter(Boolean),
    });
    showToast('Product created successfully');
    navigate('/admin/products');
  }

  return <ProductForm initialData={EMPTY_FORM} onSave={handleSave} title="Add Product" onMenuClick={onMenuClick} />;
}

// ─── EditProduct ──────────────────────────────────────────────────────────────
export function EditProduct() {
  const { onMenuClick } = useOutletContext<OutletCtx>();
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [initial, setInitial] = useState<ProductFormData | null>(null);

  useEffect(() => {
    if (!id) return;
    productService.getById(id).then((p) => {
      if (p) setInitial(productToForm(p));
      else navigate('/admin/products');
    });
  }, [id, navigate]);

  async function handleSave(data: ProductFormData) {
    if (!id) return;
    await productService.update(id, {
      name: data.name, sku: data.sku, price: Number(data.price),
      description: data.description, category: data.category as Product['category'],
      color: data.color, texture: data.texture as Product['texture'],
      length: data.length, hairType: data.hairType as Product['hairType'],
      stockQuantity: Number(data.stockQuantity),
      lowStockThreshold: Number(data.lowStockThreshold),
      status: data.status,
      images: data.images.split('\n').map((s) => s.trim()).filter(Boolean),
    });
    showToast('Product updated successfully');
    navigate('/admin/products');
  }

  if (!initial) {
    return (
      <>
        <AdminHeader title="Edit Product" onMenuClick={onMenuClick} />
        <div className="p-6">
          <div className="h-8 w-48 bg-rs-surface animate-pulse rounded" />
        </div>
      </>
    );
  }

  return <ProductForm initialData={initial} onSave={handleSave} title="Edit Product" onMenuClick={onMenuClick} />;
}
