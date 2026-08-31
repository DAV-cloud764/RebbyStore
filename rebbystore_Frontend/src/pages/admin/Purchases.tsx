import { useEffect, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { Plus, ChevronDown, ChevronUp } from 'lucide-react';
import { AdminHeader } from '../../components/admin/AdminSidebar';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { Modal } from '../../components/ui/Modal';
import { Select } from '../../components/ui/Select';
import { Input } from '../../components/ui/Input';
import { purchaseService } from '../../services/purchaseService';
import { productService } from '../../services/productService';
import { inventoryService } from '../../services/inventoryService';
import type { Purchase, PurchaseItem } from '../../types/purchase';
import type { Product } from '../../types/product';
import { useToast } from '../../contexts/ToastContext';
import { formatPrice, formatDate } from '../../utils/formatting';

interface OutletCtx { onMenuClick: () => void; }

export default function Purchases() {
  const { onMenuClick } = useOutletContext<OutletCtx>();
  const { showToast } = useToast();
  const [purchases, setPurchases] = useState<Purchase[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [expanded, setExpanded] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  const [supplier, setSupplier] = useState('');
  const [notes, setNotes] = useState('');
  const [lines, setLines] = useState<{ productId: string; quantity: string; costPerUnit: string }[]>([
    { productId: '', quantity: '', costPerUnit: '' },
  ]);

  useEffect(() => {
    purchaseService.getAll().then(setPurchases);
    productService.getAll().then(setProducts);
  }, []);

  function addLine() {
    setLines((l) => [...l, { productId: '', quantity: '', costPerUnit: '' }]);
  }

  function removeLine(i: number) {
    setLines((l) => l.filter((_, idx) => idx !== i));
  }

  function updateLine(i: number, key: string, val: string) {
    setLines((l) => l.map((ln, idx) => idx === i ? { ...ln, [key]: val } : ln));
  }

  const lineTotal = lines.reduce((s, l) => s + Number(l.quantity || 0) * Number(l.costPerUnit || 0), 0);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!supplier.trim()) { showToast('Supplier name is required', 'error'); return; }
    const validLines = lines.filter((l) => l.productId && Number(l.quantity) > 0);
    if (validLines.length === 0) { showToast('Add at least one product line', 'error'); return; }

    setSaving(true);
    try {
      const items: PurchaseItem[] = validLines.map((l) => {
        const p = products.find((pr) => pr.id === l.productId)!;
        return {
          productId: p.id, productName: p.name, sku: p.sku,
          quantity: Number(l.quantity), costPerUnit: Number(l.costPerUnit),
          totalCost: Number(l.quantity) * Number(l.costPerUnit),
        };
      });
      const totalCost = items.reduce((s, i) => s + i.totalCost, 0);
      const poNum = `PO-${Date.now().toString().slice(-4)}`;

      await purchaseService.create({
        purchaseNumber: poNum, supplier, items, totalCost,
        date: new Date().toISOString(), notes: notes || undefined, status: 'received',
      });

      // Record inventory movements for each line
      for (const item of items) {
        await inventoryService.recordMovement({
          productId: item.productId, productName: item.productName, sku: item.sku,
          type: 'in', quantity: item.quantity, reason: 'purchase',
          reference: poNum, date: new Date().toISOString(),
        });
      }

      const updated = await purchaseService.getAll();
      setPurchases(updated);
      setModalOpen(false);
      setSupplier(''); setNotes('');
      setLines([{ productId: '', quantity: '', costPerUnit: '' }]);
      showToast(`Purchase ${poNum} recorded. Stock updated.`);
    } finally {
      setSaving(false);
    }
  }

  const totalSpend = purchases.reduce((s, p) => s + p.totalCost, 0);
  const productOptions = products.map((p) => ({ value: p.id, label: `${p.name} (${p.sku})` }));

  return (
    <>
      <AdminHeader
        title="Purchases"
        onMenuClick={onMenuClick}
        actions={<Button size="sm" onClick={() => setModalOpen(true)}><Plus size={14} /> New Purchase</Button>}
      />

      <main className="p-4 md:p-6 space-y-5">
        {/* Stats */}
        <div className="grid grid-cols-2 gap-3">
          <div className="bg-white border border-rs-border p-4">
            <p className="section-label mb-1">Total Purchases</p>
            <p className="text-2xl font-display font-bold text-rs-ink">{purchases.length}</p>
          </div>
          <div className="bg-rs-ink text-white p-4 border border-rs-ink">
            <p className="text-xs tracking-wider uppercase text-white/60 mb-1">Total Spend</p>
            <p className="text-2xl font-display font-bold">{formatPrice(totalSpend)}</p>
          </div>
        </div>

        {/* List */}
        <div className="bg-white border border-rs-border divide-y divide-rs-border">
          {purchases.map((pu) => (
            <div key={pu.id}>
              <div
                className="flex items-center justify-between p-4 cursor-pointer hover:bg-rs-surface/40 transition-colors"
                onClick={() => setExpanded(expanded === pu.id ? null : pu.id)}
              >
                <div className="flex items-center gap-4">
                  <span className="font-mono text-xs font-semibold text-rs-ink">{pu.purchaseNumber}</span>
                  <span className="text-sm text-rs-muted">{pu.supplier}</span>
                  <span className="hidden sm:block text-xs text-rs-muted">{pu.items.length} item{pu.items.length !== 1 ? 's' : ''}</span>
                </div>
                <div className="flex items-center gap-4">
                  <span className="text-sm font-semibold text-rs-ink">{formatPrice(pu.totalCost)}</span>
                  <span className="text-xs text-rs-muted hidden sm:block">{formatDate(pu.date)}</span>
                  <Badge variant="in-stock">{pu.status}</Badge>
                  {expanded === pu.id ? <ChevronUp size={14} className="text-rs-muted" /> : <ChevronDown size={14} className="text-rs-muted" />}
                </div>
              </div>

              {expanded === pu.id && (
                <div className="px-4 pb-4 bg-rs-surface/30">
                  <table className="admin-table mt-2">
                    <thead><tr><th>Product</th><th>SKU</th><th>Qty</th><th>Cost/Unit</th><th>Total</th></tr></thead>
                    <tbody>
                      {pu.items.map((item) => (
                        <tr key={item.productId}>
                          <td className="text-xs">{item.productName}</td>
                          <td className="font-mono text-xs text-rs-muted">{item.sku}</td>
                          <td className="text-xs">{item.quantity}</td>
                          <td className="text-xs">{formatPrice(item.costPerUnit)}</td>
                          <td className="text-xs font-semibold">{formatPrice(item.totalCost)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                  {pu.notes && <p className="text-xs text-rs-muted mt-3 italic">Note: {pu.notes}</p>}
                </div>
              )}
            </div>
          ))}
          {purchases.length === 0 && (
            <p className="text-center py-10 text-rs-muted text-sm">No purchases recorded yet.</p>
          )}
        </div>
      </main>

      {/* New Purchase Modal */}
      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title="New Purchase" size="lg">
        <form onSubmit={handleSubmit} className="space-y-5">
          <Input label="Supplier Name" value={supplier} onChange={(e) => setSupplier(e.target.value)} required placeholder="e.g. Guangzhou Hair Trading Co." />

          {/* Line items */}
          <div>
            <div className="flex items-center justify-between mb-2">
              <label className="label-base">Products</label>
              <button type="button" onClick={addLine} className="text-xs text-rs-muted hover:text-rs-ink flex items-center gap-1 transition-colors">
                <Plus size={12} /> Add line
              </button>
            </div>
            <div className="space-y-2">
              {lines.map((line, i) => (
                <div key={i} className="grid grid-cols-12 gap-2 items-start">
                  <div className="col-span-5">
                    <Select
                      options={productOptions}
                      placeholder="Select product"
                      value={line.productId}
                      onChange={(e) => updateLine(i, 'productId', e.target.value)}
                    />
                  </div>
                  <div className="col-span-3">
                    <input type="number" min={1} value={line.quantity}
                      onChange={(e) => updateLine(i, 'quantity', e.target.value)}
                      placeholder="Qty" className="input-base text-sm" />
                  </div>
                  <div className="col-span-3">
                    <input type="number" min={0} value={line.costPerUnit}
                      onChange={(e) => updateLine(i, 'costPerUnit', e.target.value)}
                      placeholder="Cost/unit" className="input-base text-sm" />
                  </div>
                  <div className="col-span-1 pt-2.5">
                    {lines.length > 1 && (
                      <button type="button" onClick={() => removeLine(i)} className="text-rs-muted hover:text-red-500 transition-colors text-lg leading-none">×</button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>

          {lineTotal > 0 && (
            <div className="px-3 py-2.5 bg-rs-surface border border-rs-border text-sm">
              Estimated Total: <strong className="text-rs-ink">{formatPrice(lineTotal)}</strong>
            </div>
          )}

          <div>
            <label className="label-base">Notes (optional)</label>
            <textarea value={notes} onChange={(e) => setNotes(e.target.value)} rows={2} className="input-base resize-none" placeholder="Shipping details, payment terms…" />
          </div>

          <div className="flex gap-3 pt-2">
            <Button type="submit" loading={saving} className="flex-1 justify-center">Record Purchase & Update Stock</Button>
            <Button variant="secondary" type="button" onClick={() => setModalOpen(false)}>Cancel</Button>
          </div>
        </form>
      </Modal>
    </>
  );
}
