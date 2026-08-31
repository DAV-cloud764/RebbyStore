import { useEffect, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { ArrowDown, ArrowUp } from 'lucide-react';
import { AdminHeader } from '../../components/admin/AdminSidebar';
import { InventoryTable } from '../../components/admin/InventoryTable';
import { Modal } from '../../components/ui/Modal';
import { Button } from '../../components/ui/Button';
import { Select } from '../../components/ui/Select';
import { Input } from '../../components/ui/Input';
import { productService } from '../../services/productService';
import { inventoryService } from '../../services/inventoryService';
import type { Product } from '../../types/product';
import { useToast } from '../../contexts/ToastContext';
import { formatPrice } from '../../utils/formatting';

interface OutletCtx { onMenuClick: () => void; }

type MovementModal = 'none' | 'in' | 'out';

export default function Inventory() {
  const { onMenuClick } = useOutletContext<OutletCtx>();
  const { showToast } = useToast();
  const [products, setProducts] = useState<Product[]>([]);
  const [modal, setModal] = useState<MovementModal>('none');
  const [saving, setSaving] = useState(false);

  const [form, setForm] = useState({
    productId: '',
    quantity: '',
    reason: 'purchase',
    supplier: '',
    costPerUnit: '',
    notes: '',
    reference: '',
  });

  useEffect(() => { productService.getAll().then(setProducts); }, []);

  function handleChange(key: string, val: string) {
    setForm((p) => ({ ...p, [key]: val }));
  }

  function openModal(type: MovementModal) {
    setForm({ productId: '', quantity: '', reason: type === 'in' ? 'purchase' : 'sale', supplier: '', costPerUnit: '', notes: '', reference: '' });
    setModal(type);
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!form.productId || !form.quantity) {
      showToast('Product and quantity are required', 'error'); return;
    }
    const product = products.find((p) => p.id === form.productId);
    if (!product) return;

    setSaving(true);
    await inventoryService.recordMovement({
      productId: product.id,
      productName: product.name,
      sku: product.sku,
      type: modal as 'in' | 'out',
      quantity: Number(form.quantity),
      reason: form.reason as any,
      reference: form.reference || undefined,
      notes: form.notes || undefined,
      date: new Date().toISOString(),
    });

    // Refresh products
    const updated = await productService.getAll();
    setProducts(updated);
    setSaving(false);
    setModal('none');

    const cost = modal === 'in' && form.costPerUnit
      ? ` — Total cost: ${formatPrice(Number(form.costPerUnit) * Number(form.quantity))}`
      : '';
    showToast(`Stock ${modal === 'in' ? 'added' : 'removed'} successfully${cost}`);
  }

  const selectedProduct = products.find((p) => p.id === form.productId);
  const productOptions = products.map((p) => ({ value: p.id, label: `${p.name} (${p.sku})` }));

  const outReasons = [
    { value: 'sale', label: 'Sale' },
    { value: 'damaged', label: 'Damaged' },
    { value: 'lost', label: 'Lost' },
    { value: 'returned-to-supplier', label: 'Returned to Supplier' },
    { value: 'adjustment', label: 'Adjustment' },
  ];

  return (
    <>
      <AdminHeader
        title="Inventory — Stock Overview"
        onMenuClick={onMenuClick}
        actions={
          <div className="flex gap-2">
            <Button variant="secondary" size="sm" onClick={() => openModal('out')}>
              <ArrowDown size={14} /> Stock Out
            </Button>
            <Button size="sm" onClick={() => openModal('in')}>
              <ArrowUp size={14} /> Stock In
            </Button>
          </div>
        }
      />

      <main className="p-4 md:p-6">
        <div className="bg-white border border-rs-border overflow-hidden">
          <InventoryTable products={products} />
        </div>
        <p className="text-xs text-rs-muted mt-3">{products.length} products</p>
      </main>

      {/* Stock In Modal */}
      <Modal isOpen={modal === 'in'} onClose={() => setModal('none')} title="Record Stock In" size="md">
        <form onSubmit={handleSubmit} className="space-y-4">
          <Select label="Product" options={productOptions} placeholder="Select product…" value={form.productId}
            onChange={(e) => handleChange('productId', e.target.value)} required />

          {selectedProduct && (
            <div className="px-3 py-2 bg-rs-surface text-xs text-rs-muted border border-rs-border">
              Current stock: <strong className="text-rs-ink">{selectedProduct.stockQuantity}</strong> units
            </div>
          )}

          <div className="grid grid-cols-2 gap-4">
            <Input label="Quantity Received" type="number" min={1} value={form.quantity}
              onChange={(e) => handleChange('quantity', e.target.value)} required placeholder="10" />
            <Input label="Cost per Unit (TSh)" type="number" min={0} value={form.costPerUnit}
              onChange={(e) => handleChange('costPerUnit', e.target.value)} placeholder="180000" />
          </div>

          {form.quantity && form.costPerUnit && (
            <div className="px-3 py-2 bg-rs-surface border border-rs-border text-sm">
              Total Purchase Cost: <strong className="text-rs-ink">
                {formatPrice(Number(form.quantity) * Number(form.costPerUnit))}
              </strong>
            </div>
          )}

          <Input label="Supplier" value={form.supplier} onChange={(e) => handleChange('supplier', e.target.value)} placeholder="Supplier name" />
          <Input label="Reference / Purchase Order" value={form.reference} onChange={(e) => handleChange('reference', e.target.value)} placeholder="PO-006" />
          <div>
            <label className="label-base">Notes (optional)</label>
            <textarea value={form.notes} onChange={(e) => handleChange('notes', e.target.value)} rows={2}
              className="input-base resize-none" placeholder="Any additional notes…" />
          </div>

          {selectedProduct && form.quantity && (
            <div className="px-3 py-2.5 bg-green-50 border border-green-200 text-sm text-green-700">
              Stock will update: {selectedProduct.stockQuantity} → <strong>{selectedProduct.stockQuantity + Number(form.quantity || 0)}</strong>
            </div>
          )}

          <div className="flex gap-3 pt-2">
            <Button type="submit" loading={saving} className="flex-1 justify-center">Confirm Stock In</Button>
            <Button variant="secondary" type="button" onClick={() => setModal('none')}>Cancel</Button>
          </div>
        </form>
      </Modal>

      {/* Stock Out Modal */}
      <Modal isOpen={modal === 'out'} onClose={() => setModal('none')} title="Record Stock Out" size="md">
        <form onSubmit={handleSubmit} className="space-y-4">
          <Select label="Product" options={productOptions} placeholder="Select product…" value={form.productId}
            onChange={(e) => handleChange('productId', e.target.value)} required />

          {selectedProduct && (
            <div className="px-3 py-2 bg-rs-surface text-xs text-rs-muted border border-rs-border">
              Current stock: <strong className="text-rs-ink">{selectedProduct.stockQuantity}</strong> units
            </div>
          )}

          <div className="grid grid-cols-2 gap-4">
            <Input label="Quantity" type="number" min={1} max={selectedProduct?.stockQuantity}
              value={form.quantity} onChange={(e) => handleChange('quantity', e.target.value)} required placeholder="2" />
            <Select label="Reason" options={outReasons} value={form.reason}
              onChange={(e) => handleChange('reason', e.target.value)} required />
          </div>

          <Input label="Reference (Order # etc.)" value={form.reference}
            onChange={(e) => handleChange('reference', e.target.value)} placeholder="RS-001430" />
          <div>
            <label className="label-base">Notes (optional)</label>
            <textarea value={form.notes} onChange={(e) => handleChange('notes', e.target.value)} rows={2}
              className="input-base resize-none" placeholder="Any additional notes…" />
          </div>

          {selectedProduct && form.quantity && (
            <div className={`px-3 py-2.5 border text-sm ${
              Number(form.quantity) > selectedProduct.stockQuantity
                ? 'bg-red-50 border-red-200 text-red-700'
                : 'bg-amber-50 border-amber-200 text-amber-700'
            }`}>
              {Number(form.quantity) > selectedProduct.stockQuantity
                ? '⚠ Quantity exceeds current stock'
                : `Stock will update: ${selectedProduct.stockQuantity} → ${Math.max(0, selectedProduct.stockQuantity - Number(form.quantity))}`}
            </div>
          )}

          <div className="flex gap-3 pt-2">
            <Button type="submit" loading={saving}
              disabled={!!(selectedProduct && Number(form.quantity) > selectedProduct.stockQuantity)}
              className="flex-1 justify-center">
              Confirm Stock Out
            </Button>
            <Button variant="secondary" type="button" onClick={() => setModal('none')}>Cancel</Button>
          </div>
        </form>
      </Modal>
    </>
  );
}
