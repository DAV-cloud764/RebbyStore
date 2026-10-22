import { useEffect, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import type { FormEvent } from 'react';
import { Plus, ChevronDown, ChevronUp, Pencil } from 'lucide-react';

import { AdminHeader } from '../../components/admin/AdminSidebar';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { Modal } from '../../components/ui/Modal';
import { Select } from '../../components/ui/Select';
import { Input } from '../../components/ui/Input';

import { purchaseService } from '../../services/purchaseService';
import { productService } from '../../services/productService';
import { supplierService } from '../../services/supplierService';
import { ApiError } from '../../services/apiClient';

import type { Purchase } from '../../types/purchase';
import type { Product } from '../../types/product';
import type { Supplier } from '../../services/supplierService';

import { useToast } from '../../contexts/ToastContext';
import { formatPrice, formatDate } from '../../utils/formatting';

interface OutletCtx {
  onMenuClick: () => void;
}

interface PurchaseLine {
  productName: string;
  quantity: string;
  costPerUnit: string;
}

interface NewProductDraft {
  name: string;
  sku: string;
  price: string;
  category: Product['category'];
}

const today = () => new Date().toISOString().slice(0, 10);

const emptyLine = (): PurchaseLine => ({
  productName: '',
  quantity: '',
  costPerUnit: '',
});

const productCategoryOptions: { value: string; label: string }[] = [
  { value: 'human-hair', label: 'Human Hair' },
  { value: 'synthetic', label: 'Synthetic' },
  { value: 'lace-front', label: 'Lace Front' },
  { value: 'closure', label: 'Closure' },
  { value: 'bob', label: 'Bob' },
];

export default function Purchases() {
  const { onMenuClick } = useOutletContext<OutletCtx>();
  const { showToast } = useToast();

  const [purchases, setPurchases] = useState<Purchase[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [, setSuppliers] = useState<Supplier[]>([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [expanded, setExpanded] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [editingPurchase, setEditingPurchase] = useState<Purchase | null>(null);
  const [newProductForLine, setNewProductForLine] = useState<number | null>(null);
  const [newProductDraft, setNewProductDraft] = useState<NewProductDraft>({
    name: '',
    sku: '',
    price: '',
    category: 'human-hair',
  });

  const [supplierName, setSupplierName] = useState('');
  const [purchaseDate, setPurchaseDate] = useState(today());
  const [notes, setNotes] = useState('');
  const [lines, setLines] = useState<PurchaseLine[]>([emptyLine()]);

  useEffect(() => {
    async function loadData() {
      try {
        const [loadedPurchases, loadedProducts, loadedSuppliers] = await Promise.all([
          purchaseService.getAll(),
          productService.getAll(),
          supplierService.getAll(),
        ]);
        setPurchases(loadedPurchases);
        setProducts(loadedProducts);
        setSuppliers(loadedSuppliers);
      } catch (error) {
        const message = error instanceof ApiError
          ? error.message
          : 'Failed to load purchase data';
        showToast(message, 'error');
      }
    }

    void loadData();
  }, [showToast]);

  function resetForm() {
    setEditingPurchase(null);
    setSupplierName('');
    setPurchaseDate(today());
    setNotes('');
    setLines([emptyLine()]);
    setNewProductForLine(null);
    setNewProductDraft({ name: '', sku: '', price: '', category: 'human-hair' });
  }

  function addLine() {
    setLines((current) => [...current, emptyLine()]);
  }

  function removeLine(index: number) {
    setLines((current) => current.filter((_, i) => i !== index));
    setNewProductForLine((current) => {
      if (current === null) return null;
      if (current === index) return null;
      return current > index ? current - 1 : current;
    });
  }

  function updateLine(index: number, key: keyof PurchaseLine, value: string) {
    setLines((current) => current.map((line, i) => (
      i === index ? { ...line, [key]: value } : line
    )));
  }

  function findProductByNameOrSku(value: string): Product | undefined {
    const normalized = value.trim().toLocaleLowerCase();
    if (!normalized) return undefined;
    return products.find((product) => (
      product.name.trim().toLocaleLowerCase() === normalized
      || product.sku.trim().toLocaleLowerCase() === normalized
    ));
  }

  function openNewPurchase() {
    resetForm();
    setModalOpen(true);
  }

  function openEditPurchase(purchase: Purchase) {
    setEditingPurchase(purchase);
    setSupplierName(purchase.supplier);
    setPurchaseDate(purchase.date);
    setNotes(purchase.notes ?? '');
    setLines(purchase.items.map((item) => ({
      productName: item.productName,
      quantity: String(item.quantity),
      costPerUnit: String(item.costPerUnit),
    })));
    setNewProductForLine(null);
    setModalOpen(true);
  }

  async function handleAddProductForLine(index: number) {
    const name = newProductDraft.name.trim();
    const sku = newProductDraft.sku.trim();
    const price = Number(newProductDraft.price);

    if (!name || !sku || !Number.isFinite(price) || price <= 0) {
      showToast('Enter the product name, SKU, and a valid selling price.', 'error');
      return;
    }

    setSaving(true);
    try {
      const createdProduct = await productService.create({
        name,
        sku,
        price,
        images: [],
        description: '',
        category: newProductDraft.category,
        color: '',
        texture: 'straight',
        length: '',
        hairType: 'human',
        stockQuantity: 0,
        lowStockThreshold: 5,
        status: 'active',
      });

      setProducts((current) => {
        const exists = current.some((product) => product.id === createdProduct.id);
        return exists ? current : [...current, createdProduct];
      });
      updateLine(index, 'productName', createdProduct.name);
      setNewProductForLine(null);
      setNewProductDraft({ name: '', sku: '', price: '', category: 'human-hair' });
      showToast(`Product "${createdProduct.name}" added to the catalogue.`, 'success');
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Could not add the product.';
      showToast(message, 'error');
    } finally {
      setSaving(false);
    }
  }

  const validLines = lines.filter((line) => (
    line.productName.trim()
    && Number(line.quantity) > 0
    && Number(line.costPerUnit) > 0
  ));

  const lineTotal = validLines.reduce((sum, line) => (
    sum + Number(line.quantity) * Number(line.costPerUnit)
  ), 0);

  async function handleSubmit(event: FormEvent) {
  event.preventDefault();

  if (!supplierName.trim()) {
    showToast('Supplier name is required.', 'error');
    return;
  }

  if (!purchaseDate) {
    showToast('Purchase date is required.', 'error');
    return;
  }

  const partiallyCompletedLine = lines.some((line) => {
    const hasAnyValue = Boolean(
      line.productName.trim() ||
      line.quantity.trim() ||
      line.costPerUnit.trim(),
    );

    const isComplete = Boolean(
      line.productName.trim() &&
      Number(line.quantity) > 0 &&
      Number(line.costPerUnit) > 0,
    );

    return hasAnyValue && !isComplete;
  });

  if (partiallyCompletedLine) {
    showToast(
      'Complete or remove each partially filled product line.',
      'error',
    );
    return;
  }

  if (validLines.length === 0) {
    showToast('Add at least one valid product line.', 'error');
    return;
  }

  const resolvedLines = validLines.map((line) => ({
    line,
    product: findProductByNameOrSku(line.productName),
  }));

  const missingProduct = resolvedLines.find(
    (entry) => !entry.product,
  );

  if (missingProduct) {
    showToast(
      `Product "${missingProduct.line.productName.trim()}" is not in the catalogue. Add it first.`,
      'error',
    );
    return;
  }

  setSaving(true);

  try {
    const normalizedSupplierName = supplierName.trim();
    const normalizedKey = normalizedSupplierName.toLowerCase();

    // Refresh suppliers to avoid creating a duplicate record.
    const latestSuppliers = await supplierService.getAll();
    setSuppliers(latestSuppliers);

    let supplier = latestSuppliers.find(
      (item) => item.name.trim().toLowerCase() === normalizedKey,
    );

    if (!supplier) {
      try {
        supplier = await supplierService.create({
          name: normalizedSupplierName,
        });
      } catch (createError) {
        // A duplicate may have been created by another request.
        if (
          !(createError instanceof ApiError) ||
          createError.status !== 409
        ) {
          throw createError;
        }

        const refreshedSuppliers = await supplierService.getAll();
        setSuppliers(refreshedSuppliers);

        supplier = refreshedSuppliers.find(
          (item) =>
            item.name.trim().toLowerCase() === normalizedKey,
        );

        if (!supplier) {
          throw createError;
        }
      }
    }

    if (!supplier) {
      throw new Error('Could not find or create the supplier.');
    }

    const resolvedSupplier = supplier;

    setSuppliers((current) => {
      if (
        current.some((item) => item.id === resolvedSupplier.id)
      ) {
        return current;
      }

      return [...current, resolvedSupplier].sort(
        (a, b) => a.name.localeCompare(b.name),
      );
    });

    const purchaseItems = resolvedLines.map(({ line, product }) => ({
      productId: product!.id,
      quantity: Number(line.quantity),
      unitCost: Number(line.costPerUnit),
    }));

    if (editingPurchase) {
      const updatedPurchase = await purchaseService.update(
        editingPurchase.id,
        {
          supplierId: resolvedSupplier.id,
          purchaseDate,
          notes: notes.trim() || undefined,
          items: purchaseItems,
        },
      );

      setPurchases(await purchaseService.getAll());
      setExpanded(updatedPurchase.id);

      showToast(
        `Purchase ${updatedPurchase.purchaseNumber} updated.`,
        'success',
      );
    } else {
      const receivedPurchase = await purchaseService.createAndReceive({
        supplierId: resolvedSupplier.id,
        purchaseDate,
        notes: notes.trim() || undefined,
        items: purchaseItems,
      });

      setPurchases(await purchaseService.getAll());

      showToast(
        `Purchase ${receivedPurchase.purchaseNumber} received. Stock updated.`,
        'success',
      );
    }

    setModalOpen(false);
    resetForm();
  } catch (error) {
    const message =
      error instanceof ApiError
        ? error.message
        : error instanceof Error
          ? error.message
          : editingPurchase
            ? 'Failed to update purchase.'
            : 'Failed to record purchase.';

    showToast(message, 'error');
  } finally {
    setSaving(false);
  }
}

  const totalSpend = purchases
    .filter((purchase) => purchase.status === 'received')
    .reduce((sum, purchase) => sum + purchase.totalCost, 0);

  function statusVariant(status: Purchase['status']): 'in-stock' | 'low-stock' | 'out-of-stock' {
    switch (status) {
      case 'received': return 'in-stock';
      case 'ordered':
      case 'draft': return 'low-stock';
      case 'cancelled': return 'out-of-stock';
    }
  }

  return (
    <>
      <AdminHeader
        title="Purchases"
        onMenuClick={onMenuClick}
        actions={
          <Button size="sm" onClick={openNewPurchase}>
            <Plus size={14} />
            New Purchase
          </Button>
        }
      />

      <main className="p-4 md:p-6 space-y-5">
        <div className="grid grid-cols-2 gap-3">
          <div className="bg-white border border-rs-border p-4">
            <p className="section-label mb-1">Total Purchases</p>
            <p className="text-2xl font-display font-bold text-rs-ink">{purchases.length}</p>
          </div>
          <div className="bg-rs-ink text-white p-4 border border-rs-ink">
            <p className="text-xs tracking-wider uppercase text-white/60 mb-1">Received Purchase Spend</p>
            <p className="text-2xl font-display font-bold">{formatPrice(totalSpend)}</p>
          </div>
        </div>

        <div className="bg-white border border-rs-border divide-y divide-rs-border">
          {purchases.map((purchase) => (
            <div key={purchase.id}>
              <div
                className="flex items-center justify-between p-4 cursor-pointer hover:bg-rs-surface/40 transition-colors"
                onClick={() => setExpanded(expanded === purchase.id ? null : purchase.id)}
              >
                <div className="flex items-center gap-4 min-w-0">
                  <span className="font-mono text-xs font-semibold text-rs-ink">{purchase.purchaseNumber}</span>
                  <span className="text-sm text-rs-muted truncate">{purchase.supplier}</span>
                  <span className="hidden sm:block text-xs text-rs-muted">
                    {purchase.items.length} item{purchase.items.length !== 1 ? 's' : ''}
                  </span>
                </div>
                <div className="flex items-center gap-4 shrink-0">
                  <span className="text-sm font-semibold text-rs-ink">{formatPrice(purchase.totalCost)}</span>
                  <span className="text-xs text-rs-muted hidden sm:block">{formatDate(purchase.date)}</span>
                  <Badge variant={statusVariant(purchase.status)}>{purchase.status}</Badge>
                  {expanded === purchase.id ? (
                    <ChevronUp size={14} className="text-rs-muted" />
                  ) : (
                    <ChevronDown size={14} className="text-rs-muted" />
                  )}
                </div>
              </div>

              {expanded === purchase.id && (
                <div className="px-4 pb-4 bg-rs-surface/30">
                  <div className="flex justify-end pt-3">
                    {purchase.status !== 'cancelled' && (
                      <Button size="sm" variant="secondary" onClick={() => openEditPurchase(purchase)}>
                        <Pencil size={13} /> Edit Purchase
                      </Button>
                    )}
                  </div>
                  <table className="admin-table mt-2">
                    <thead>
                      <tr>
                        <th>Product</th>
                        <th>SKU</th>
                        <th>Qty</th>
                        <th>Cost/Unit</th>
                        <th>Total</th>
                      </tr>
                    </thead>
                    <tbody>
                      {purchase.items.map((item) => (
                        <tr key={item.id ?? `${purchase.id}-${item.productId}`}>
                          <td className="text-xs">{item.productName}</td>
                          <td className="font-mono text-xs text-rs-muted">{item.sku}</td>
                          <td className="text-xs">{item.quantity}</td>
                          <td className="text-xs">{formatPrice(item.costPerUnit)}</td>
                          <td className="text-xs font-semibold">{formatPrice(item.totalCost)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                  {purchase.notes && (
                    <p className="text-xs text-rs-muted mt-3 italic">Note: {purchase.notes}</p>
                  )}
                  {purchase.status === 'received' && (
                    <p className="text-xs text-rs-muted mt-2">
                      Editing received quantities or products also adjusts inventory. The update is rejected if it would require removing more stock than is available.
                    </p>
                  )}
                </div>
              )}
            </div>
          ))}
          {purchases.length === 0 && (
            <p className="text-center py-10 text-rs-muted text-sm">No purchases recorded yet.</p>
          )}
        </div>
      </main>

      <Modal
        isOpen={modalOpen}
        onClose={() => {
          if (!saving) {
            setModalOpen(false);
            resetForm();
          }
        }}
        title={editingPurchase ? `Edit ${editingPurchase.purchaseNumber}` : 'New Purchase'}
        size="lg"
      >
        <form onSubmit={handleSubmit} className="space-y-5">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Input
              label="Supplier Name"
              value={supplierName}
              onChange={(event) => setSupplierName(event.target.value)}
              placeholder="Type supplier name"
              maxLength={150}
              required
            />
            <Input
              label="Purchase Date"
              type="date"
              value={purchaseDate}
              onChange={(event) => setPurchaseDate(event.target.value)}
              required
            />
          </div>

          {editingPurchase ? (
            <div className="px-3 py-2.5 bg-rs-surface border border-rs-border text-xs text-rs-muted">
              Update the supplier, date, notes, quantities, products, or purchase costs. If the purchase has already been received, inventory changes will be reconciled automatically.
            </div>
          ) : (
            <div className="px-3 py-2.5 bg-rs-surface border border-rs-border text-xs text-rs-muted">
              This records the purchase, marks it ordered, then receives it. Stock is updated when it is received.
            </div>
          )}

          <div>
            <div className="flex items-center justify-between mb-2">
              <label className="label-base">Products</label>
              <button
                type="button"
                onClick={addLine}
                className="text-xs text-rs-muted hover:text-rs-ink flex items-center gap-1 transition-colors"
              >
                <Plus size={12} /> Add line
              </button>
            </div>

            <div className="space-y-3">
              {lines.map((line, index) => {
                const matchedProduct = findProductByNameOrSku(line.productName);
                const listId = `purchase-product-options-${index}`;
                return (
                  <div key={index} className="space-y-2">
                    <div className="grid grid-cols-12 gap-2 items-start">
                      <div className="col-span-5">
                        <Input
                          value={line.productName}
                          onChange={(event) => updateLine(index, 'productName', event.target.value)}
                          placeholder="Type product name or SKU"
                          list={listId}
                          required={index === 0}
                        />
                        <datalist id={listId}>
                          {products.map((product) => (
                            <option key={product.id} value={product.name} label={`SKU: ${product.sku}`} />
                          ))}
                          {products.map((product) => (
                            <option key={`${product.id}-sku`} value={product.sku} label={product.name} />
                          ))}
                        </datalist>
                        {!matchedProduct && line.productName.trim() && (
                          <button
                            type="button"
                            className="mt-1 text-xs text-rs-ink underline underline-offset-2"
                            onClick={() => {
                              setNewProductForLine(index);
                              setNewProductDraft({
                                name: line.productName.trim(),
                                sku: '',
                                price: '',
                                category: 'human-hair',
                              });
                            }}
                          >
                            + Add new product to catalogue
                          </button>
                        )}
                        {matchedProduct && (
                          <p className="mt-1 text-[11px] text-rs-muted">Matched: {matchedProduct.name} · {matchedProduct.sku}</p>
                        )}
                      </div>

                      <div className="col-span-3">
                        <Input
                          type="number"
                          min={1}
                          step={1}
                          value={line.quantity}
                          onChange={(event) => updateLine(index, 'quantity', event.target.value)}
                          placeholder="Qty"
                          required={index === 0}
                        />
                      </div>

                      <div className="col-span-3">
                        <Input
                          type="number"
                          min={0.01}
                          step="0.01"
                          value={line.costPerUnit}
                          onChange={(event) => updateLine(index, 'costPerUnit', event.target.value)}
                          placeholder="Cost/unit"
                          required={index === 0}
                        />
                      </div>

                      <div className="col-span-1 pt-2.5">
                        {lines.length > 1 && (
                          <button
                            type="button"
                            onClick={() => removeLine(index)}
                            className="text-rs-muted hover:text-red-500 transition-colors text-lg leading-none"
                            aria-label="Remove product line"
                          >×</button>
                        )}
                      </div>
                    </div>

                    {newProductForLine === index && (
                      <div className="ml-1 p-3 border border-rs-border bg-white space-y-3">
                        <div className="flex items-center justify-between gap-3">
                          <p className="text-sm font-semibold text-rs-ink">Add product to catalogue</p>
                          <button
                            type="button"
                            onClick={() => setNewProductForLine(null)}
                            className="text-xs text-rs-muted underline"
                          >Cancel</button>
                        </div>
                        <p className="text-xs text-rs-muted">
                          The selling price is for the Products catalogue. The cost you enter on the purchase line remains this supplier's purchase cost.
                        </p>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                          <Input
                            label="Product Name"
                            value={newProductDraft.name}
                            onChange={(event) => setNewProductDraft((current) => ({ ...current, name: event.target.value }))}
                            required
                          />
                          <Input
                            label="SKU / Product Code"
                            value={newProductDraft.sku}
                            onChange={(event) => setNewProductDraft((current) => ({ ...current, sku: event.target.value }))}
                            placeholder="e.g. SHAMPOO-250"
                            maxLength={50}
                            required
                          />
                          <Input
                            label="Selling Price"
                            type="number"
                            min={0.01}
                            step="0.01"
                            value={newProductDraft.price}
                            onChange={(event) => setNewProductDraft((current) => ({ ...current, price: event.target.value }))}
                            required
                          />
                          <Select
                            label="Category"
                            options={productCategoryOptions}
                            value={newProductDraft.category}
                            onChange={(event) => setNewProductDraft((current) => ({
                              ...current,
                              category: event.target.value as Product['category'],
                            }))}
                            required
                          />
                        </div>
                        <div className="flex justify-end">
                          <Button
                            type="button"
                            size="sm"
                            loading={saving}
                            onClick={() => void handleAddProductForLine(index)}
                          >Add Product</Button>
                        </div>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          </div>

          {lineTotal > 0 && (
            <div className="px-3 py-2.5 bg-rs-surface border border-rs-border text-sm">
              Estimated Total: <strong className="text-rs-ink">{formatPrice(lineTotal)}</strong>
            </div>
          )}

          <div>
            <label className="label-base" htmlFor="purchase-notes">Notes (optional)</label>
            <textarea
              id="purchase-notes"
              value={notes}
              onChange={(event) => setNotes(event.target.value)}
              rows={2}
              className="input-base resize-none"
              placeholder="Shipping details, payment terms…"
              maxLength={2000}
            />
          </div>

          <div className="flex gap-3 pt-2">
            <Button type="submit" loading={saving} className="flex-1 justify-center">
              {editingPurchase ? 'Save Purchase Changes' : 'Record Purchase & Update Stock'}
            </Button>
            <Button
              variant="secondary"
              type="button"
              disabled={saving}
              onClick={() => {
                setModalOpen(false);
                resetForm();
              }}
            >Cancel</Button>
          </div>
        </form>
      </Modal>
    </>
  );
}
