import { useState, useEffect } from 'react';
import { useOutletContext, useNavigate, useParams } from 'react-router-dom';
import { AdminHeader } from '../../components/admin/AdminSidebar';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Select } from '../../components/ui/Select';
import { productService } from '../../services/productService';
import { inventoryService } from '../../services/inventoryService';
import type { Product } from '../../types/product';
import { useToast } from '../../contexts/ToastContext';
import { storeConfig } from '../../config/store';
import { productImageService } from "../../services/productImageService";

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


async function uploadSelectedImages(
  productId: string,
  files: File[],
  startingSortOrder = 0,
  alreadyHasPrimary = false,
): Promise<string[]> {
  const failedFiles: string[] = [];
  let sortOrder = startingSortOrder;
  let hasPrimary = alreadyHasPrimary;

  // Upload sequentially so primary-image updates don't race.
  for (const file of files) {
    try {
      await productImageService.upload(
        productId,
        file,
        sortOrder,
        !hasPrimary,
      );

      sortOrder += 1;
      hasPrimary = true;
    } catch {
      failedFiles.push(file.name);
    }
  }

  return failedFiles;
}


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
onSave: (
  data: ProductFormData,
  imageFiles: File[],
) => Promise<void>;
  title: string;
  onMenuClick: () => void;
}) {
  const [form, setForm] = useState<ProductFormData>(initialData);
  const [saving, setSaving] = useState(false);
  const navigate = useNavigate();

const [selectedImages, setSelectedImages] = useState<File[]>([]);
const [previewUrls, setPreviewUrls] = useState<string[]>([]);
const [imageError, setImageError] = useState<string | null>(null);
const [submitError, setSubmitError] = useState<string | null>(null);

useEffect(() => {
  const urls = selectedImages.map((file) =>
    URL.createObjectURL(file),
  );

  setPreviewUrls(urls);

  return () => {
    urls.forEach((url) => URL.revokeObjectURL(url));
  };
}, [selectedImages]);

  function handleChange(key: keyof ProductFormData, value: string) {
    setForm((p) => ({ ...p, [key]: value }));
  }


async function handleSubmit(e: React.FormEvent) {
  e.preventDefault();
  setSaving(true);
  setSubmitError(null);

  try {
    await onSave(form, selectedImages);
  } catch (error) {
    setSubmitError(
      error instanceof Error
        ? error.message
        : "Unable to save the product.",
    );
  } finally {
    setSaving(false);
  }
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


{/* Product Images */}
<div className="bg-white border border-rs-border p-5 space-y-4">
  <div>
    <h2 className="font-display font-semibold text-rs-ink">
      Product Images
    </h2>

    <p className="mt-1 text-xs text-rs-muted">
      Choose one or more product photos. Images will upload
      after the product is saved.
    </p>
  </div>

  <div className="border border-dashed border-rs-border bg-rs-surface p-5">
    <label
      htmlFor="product-images"
      className="block text-sm font-medium text-rs-ink"
    >
      Select product images

      <input
        id="product-images"
        type="file"
        accept="image/*"
        multiple
        disabled={saving}
        className="mt-3 block w-full text-sm"
        onChange={(event) => {
          const input = event.currentTarget;
          const files = Array.from(input.files ?? []);

          const validFiles = files.filter(
            (file) =>
              file.type.startsWith("image/") &&
              file.size <= 10 * 1024 * 1024,
          );

          if (validFiles.length !== files.length) {
            setImageError(
              "Only image files up to 10 MB each are accepted.",
            );
          } else {
            setImageError(null);
          }

          if (validFiles.length > 0) {
            setSelectedImages((current) => [
              ...current,
              ...validFiles,
            ]);
          }

          // Allow selecting the same file again if needed.
          input.value = "";
        }}
      />
    </label>
  </div>

  {imageError && (
    <p className="text-sm text-red-600" role="alert">
      {imageError}
    </p>
  )}

  {selectedImages.length > 0 && (
    <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
      {selectedImages.map((file, index) => (
        <div
          key={`${file.name}-${file.lastModified}-${index}`}
          className="overflow-hidden border border-rs-border rounded"
        >
          {previewUrls[index] && (
            <img
              src={previewUrls[index]}
              alt={file.name}
              className="h-32 w-full object-cover"
            />
          )}

          <div className="p-3 space-y-2">
            <p
              className="text-xs text-rs-ink truncate"
              title={file.name}
            >
              {file.name}
            </p>

            <p className="text-xs text-rs-muted">
              {(file.size / (1024 * 1024)).toFixed(2)} MB
            </p>

            <button
              type="button"
              disabled={saving}
              className="text-xs text-red-600 hover:underline disabled:opacity-50"
              onClick={() => {
                setSelectedImages((current) =>
                  current.filter((_, i) => i !== index),
                );
              }}
            >
              Remove
            </button>
          </div>
        </div>
      ))}
    </div>
  )}

  {submitError && (
    <p className="text-sm text-red-600" role="alert">
      {submitError}
    </p>
  )}
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


async function handleSave(
  data: ProductFormData,
  imageFiles: File[],
) {
  const product = await productService.create({
    name: data.name,
    sku: data.sku,
    price: Number(data.price),
    description: data.description,
    category: data.category as Product["category"],
    color: data.color,
    texture: data.texture as Product["texture"],
    length: data.length,
    hairType: data.hairType as Product["hairType"],
    stockQuantity: 0,
    lowStockThreshold: Number(data.lowStockThreshold),
    status: "active",
    images: [],
  });

  const initialStock = Number(data.stockQuantity);

  if (initialStock > 0) {
    await inventoryService.stockIn(
      product.id,
      initialStock,
    );
  }

  const failedImages = await uploadSelectedImages(
    product.id,
    imageFiles,
  );

  if (failedImages.length > 0) {
    showToast(
      `Product created, but ${failedImages.length} image upload(s) failed. Edit the product to retry.`,
    );
  } else {
    showToast("Product created successfully");
  }

  navigate("/admin/products");
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


async function handleSave(
  data: ProductFormData,
  imageFiles: File[],
) {
  if (!id) return;

  await productService.update(id, {
    name: data.name,
    sku: data.sku,
    price: Number(data.price),
    description: data.description,
    category: data.category as Product["category"],
    color: data.color,
    texture: data.texture as Product["texture"],
    length: data.length,
    hairType: data.hairType as Product["hairType"],
    stockQuantity: Number(data.stockQuantity),
    lowStockThreshold: Number(data.lowStockThreshold),
    status: data.status,
  });

  let failedImages: string[] = [];

  if (imageFiles.length > 0) {
    const existingImages =
      await productImageService.getByProduct(id);

    const nextSortOrder =
      existingImages.reduce(
        (max, image) => Math.max(max, image.sortOrder),
        -1,
      ) + 1;

    const hasPrimary = existingImages.some(
      (image) => image.primary,
    );

    failedImages = await uploadSelectedImages(
      id,
      imageFiles,
      nextSortOrder,
      hasPrimary,
    );
  }

  if (failedImages.length > 0) {
    showToast(
      `Product updated, but ${failedImages.length} image upload(s) failed. Edit the product to retry.`,
    );
  } else {
    showToast("Product updated successfully");
  }

  navigate("/admin/products");
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
