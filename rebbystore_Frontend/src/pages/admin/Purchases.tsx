import { useEffect, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import type { FormEvent } from 'react';
import { Plus, ChevronDown, ChevronUp } from 'lucide-react';

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
import {
  formatPrice,
  formatDate,
} from '../../utils/formatting';

interface OutletCtx {
  onMenuClick: () => void;
}

interface PurchaseLine {
  productId: string;
  quantity: string;
  costPerUnit: string;
}

const today = () =>
  new Date().toISOString().slice(0, 10);

export default function Purchases() {
  const { onMenuClick } =
    useOutletContext<OutletCtx>();

  const { showToast } = useToast();

  const [purchases, setPurchases] =
    useState<Purchase[]>([]);

  const [products, setProducts] =
    useState<Product[]>([]);

  const [suppliers, setSuppliers] =
    useState<Supplier[]>([]);

  const [modalOpen, setModalOpen] =
    useState(false);

  const [expanded, setExpanded] =
    useState<string | null>(null);

  const [saving, setSaving] =
    useState(false);

  const [supplierId, setSupplierId] =
    useState('');

  const [purchaseDate, setPurchaseDate] =
    useState(today());

  const [notes, setNotes] =
    useState('');

  const [lines, setLines] =
    useState<PurchaseLine[]>([
      {
        productId: '',
        quantity: '',
        costPerUnit: '',
      },
    ]);

  useEffect(() => {
    async function loadData() {
      try {
        const [
          loadedPurchases,
          loadedProducts,
          loadedSuppliers,
        ] = await Promise.all([
          purchaseService.getAll(),
          productService.getAll(),
          supplierService.getAll(),
        ]);

        setPurchases(loadedPurchases);
        setProducts(loadedProducts);
        setSuppliers(loadedSuppliers);
      } catch (error) {
        const message =
          error instanceof ApiError
            ? error.message
            : 'Failed to load purchase data';

        showToast(message, 'error');
      }
    }

    void loadData();
  }, [showToast]);

  function resetForm() {
    setSupplierId('');
    setPurchaseDate(today());
    setNotes('');
    setLines([
      {
        productId: '',
        quantity: '',
        costPerUnit: '',
      },
    ]);
  }

  function addLine() {
    setLines((current) => [
      ...current,
      {
        productId: '',
        quantity: '',
        costPerUnit: '',
      },
    ]);
  }

  function removeLine(index: number) {
    setLines((current) =>
      current.filter((_, i) => i !== index),
    );
  }

  function updateLine(
    index: number,
    key: keyof PurchaseLine,
    value: string,
  ) {
    setLines((current) =>
      current.map((line, i) =>
        i === index
          ? { ...line, [key]: value }
          : line,
      ),
    );
  }

  const validLines = lines.filter(
    (line) =>
      line.productId &&
      Number(line.quantity) > 0 &&
      Number(line.costPerUnit) > 0,
  );

  const lineTotal = validLines.reduce(
    (sum, line) =>
      sum +
      Number(line.quantity) *
        Number(line.costPerUnit),
    0,
  );

  async function handleSubmit(
    event: FormEvent,
  ) {
    event.preventDefault();

    if (!supplierId) {
      showToast(
        'Supplier is required',
        'error',
      );
      return;
    }

    if (!purchaseDate) {
      showToast(
        'Purchase date is required',
        'error',
      );
      return;
    }

    if (validLines.length === 0) {
      showToast(
        'Add at least one valid product line',
        'error',
      );
      return;
    }

    setSaving(true);

    try {
      const receivedPurchase =
        await purchaseService.createAndReceive({
          supplierId,
          purchaseDate,
          notes: notes.trim() || undefined,
          items: validLines.map((line) => ({
            productId: line.productId,
            quantity: Number(line.quantity),
            unitCost: Number(line.costPerUnit),
          })),
        });

      const updatedPurchases =
        await purchaseService.getAll();

      setPurchases(updatedPurchases);
      setModalOpen(false);
      resetForm();

      showToast(
        `Purchase ${receivedPurchase.purchaseNumber} received. Stock updated.`,
      );
    } catch (error) {
      const message =
        error instanceof ApiError
          ? error.message
          : 'Failed to record purchase';

      showToast(message, 'error');
    } finally {
      setSaving(false);
    }
  }

  const totalSpend = purchases
    .filter((purchase) => purchase.status === 'received')
    .reduce(
      (sum, purchase) =>
        sum + purchase.totalCost,
      0,
    );

  const supplierOptions = suppliers.map(
    (supplier) => ({
      value: supplier.id,
      label: supplier.name,
    }),
  );

  const productOptions = products.map(
    (product) => ({
      value: product.id,
      label: `${product.name} (${product.sku})`,
    }),
  );

  function statusVariant(
    status: Purchase['status'],
  ): 'in-stock' | 'low-stock' | 'out-of-stock' {
    switch (status) {
      case 'received':
        return 'in-stock';

      case 'ordered':
      case 'draft':
        return 'low-stock';

      case 'cancelled':
        return 'out-of-stock';
    }
  }

  return (
    <>
      <AdminHeader
        title="Purchases"
        onMenuClick={onMenuClick}
        actions={
          <Button
            size="sm"
            onClick={() => {
              resetForm();
              setModalOpen(true);
            }}
          >
            <Plus size={14} />
            New Purchase
          </Button>
        }
      />

      <main className="p-4 md:p-6 space-y-5">
        <div className="grid grid-cols-2 gap-3">
          <div className="bg-white border border-rs-border p-4">
            <p className="section-label mb-1">
              Total Purchases
            </p>
            <p className="text-2xl font-display font-bold text-rs-ink">
              {purchases.length}
            </p>
          </div>

          <div className="bg-rs-ink text-white p-4 border border-rs-ink">
            <p className="text-xs tracking-wider uppercase text-white/60 mb-1">
              Received Purchase Spend
            </p>
            <p className="text-2xl font-display font-bold">
              {formatPrice(totalSpend)}
            </p>
          </div>
        </div>

        <div className="bg-white border border-rs-border divide-y divide-rs-border">
          {purchases.map((purchase) => (
            <div key={purchase.id}>
              <div
                className="flex items-center justify-between p-4 cursor-pointer hover:bg-rs-surface/40 transition-colors"
                onClick={() =>
                  setExpanded(
                    expanded === purchase.id
                      ? null
                      : purchase.id,
                  )
                }
              >
                <div className="flex items-center gap-4 min-w-0">
                  <span className="font-mono text-xs font-semibold text-rs-ink">
                    {purchase.purchaseNumber}
                  </span>

                  <span className="text-sm text-rs-muted truncate">
                    {purchase.supplier}
                  </span>

                  <span className="hidden sm:block text-xs text-rs-muted">
                    {purchase.items.length}{' '}
                    item
                    {purchase.items.length !== 1
                      ? 's'
                      : ''}
                  </span>
                </div>

                <div className="flex items-center gap-4 shrink-0">
                  <span className="text-sm font-semibold text-rs-ink">
                    {formatPrice(
                      purchase.totalCost,
                    )}
                  </span>

                  <span className="text-xs text-rs-muted hidden sm:block">
                    {formatDate(purchase.date)}
                  </span>

                  <Badge
                    variant={statusVariant(
                      purchase.status,
                    )}
                  >
                    {purchase.status}
                  </Badge>

                  {expanded === purchase.id ? (
                    <ChevronUp
                      size={14}
                      className="text-rs-muted"
                    />
                  ) : (
                    <ChevronDown
                      size={14}
                      className="text-rs-muted"
                    />
                  )}
                </div>
              </div>

              {expanded === purchase.id && (
                <div className="px-4 pb-4 bg-rs-surface/30">
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
                      {purchase.items.map(
                        (item) => (
                          <tr
                            key={
                              item.id ??
                              `${purchase.id}-${item.productId}`
                            }
                          >
                            <td className="text-xs">
                              {item.productName}
                            </td>

                            <td className="font-mono text-xs text-rs-muted">
                              {item.sku}
                            </td>

                            <td className="text-xs">
                              {item.quantity}
                            </td>

                            <td className="text-xs">
                              {formatPrice(
                                item.costPerUnit,
                              )}
                            </td>

                            <td className="text-xs font-semibold">
                              {formatPrice(
                                item.totalCost,
                              )}
                            </td>
                          </tr>
                        ),
                      )}
                    </tbody>
                  </table>

                  {purchase.notes && (
                    <p className="text-xs text-rs-muted mt-3 italic">
                      Note: {purchase.notes}
                    </p>
                  )}
                </div>
              )}
            </div>
          ))}

          {purchases.length === 0 && (
            <p className="text-center py-10 text-rs-muted text-sm">
              No purchases recorded yet.
            </p>
          )}
        </div>
      </main>

      <Modal
        isOpen={modalOpen}
        onClose={() => {
          if (!saving) {
            setModalOpen(false);
          }
        }}
        title="New Purchase"
        size="lg"
      >
        <form
          onSubmit={handleSubmit}
          className="space-y-5"
        >
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Select
              label="Supplier"
              options={supplierOptions}
              placeholder="Select supplier"
              value={supplierId}
              onChange={(event) =>
                setSupplierId(event.target.value)
              }
              required
            />

            <Input
              label="Purchase Date"
              type="date"
              value={purchaseDate}
              onChange={(event) =>
                setPurchaseDate(
                  event.target.value,
                )
              }
              required
            />
          </div>

          <div className="px-3 py-2.5 bg-rs-surface border border-rs-border text-xs text-rs-muted">
            This action creates the purchase as
            <strong className="text-rs-ink">
              {' '}
              draft
            </strong>
            , adds the products, changes it to
            <strong className="text-rs-ink">
              {' '}
              ordered
            </strong>
            , then
            <strong className="text-rs-ink">
              {' '}
              receives
            </strong>
            it. Stock is updated when it is received.
          </div>

          <div>
            <div className="flex items-center justify-between mb-2">
              <label className="label-base">
                Products
              </label>

              <button
                type="button"
                onClick={addLine}
                className="text-xs text-rs-muted hover:text-rs-ink flex items-center gap-1 transition-colors"
              >
                <Plus size={12} />
                Add line
              </button>
            </div>

            <div className="space-y-2">
              {lines.map((line, index) => (
                <div
                  key={index}
                  className="grid grid-cols-12 gap-2 items-start"
                >
                  <div className="col-span-5">
                    <Select
                      options={productOptions}
                      placeholder="Select product"
                      value={line.productId}
                      onChange={(event) =>
                        updateLine(
                          index,
                          'productId',
                          event.target.value,
                        )
                      }
                    />
                  </div>

                  <div className="col-span-3">
                    <input
                      type="number"
                      min={1}
                      value={line.quantity}
                      onChange={(event) =>
                        updateLine(
                          index,
                          'quantity',
                          event.target.value,
                        )
                      }
                      placeholder="Qty"
                      className="input-base text-sm"
                    />
                  </div>

                  <div className="col-span-3">
                    <input
                      type="number"
                      min={0.01}
                      step="0.01"
                      value={line.costPerUnit}
                      onChange={(event) =>
                        updateLine(
                          index,
                          'costPerUnit',
                          event.target.value,
                        )
                      }
                      placeholder="Cost/unit"
                      className="input-base text-sm"
                    />
                  </div>

                  <div className="col-span-1 pt-2.5">
                    {lines.length > 1 && (
                      <button
                        type="button"
                        onClick={() =>
                          removeLine(index)
                        }
                        className="text-rs-muted hover:text-red-500 transition-colors text-lg leading-none"
                        aria-label="Remove product line"
                      >
                        ×
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>

          {lineTotal > 0 && (
            <div className="px-3 py-2.5 bg-rs-surface border border-rs-border text-sm">
              Estimated Total:{' '}
              <strong className="text-rs-ink">
                {formatPrice(lineTotal)}
              </strong>
            </div>
          )}

          <div>
            <label className="label-base">
              Notes (optional)
            </label>

            <textarea
              value={notes}
              onChange={(event) =>
                setNotes(event.target.value)
              }
              rows={2}
              className="input-base resize-none"
              placeholder="Shipping details, payment terms…"
              maxLength={2000}
            />
          </div>

          <div className="flex gap-3 pt-2">
            <Button
              type="submit"
              loading={saving}
              className="flex-1 justify-center"
            >
              Record Purchase & Update Stock
            </Button>

            <Button
              variant="secondary"
              type="button"
              disabled={saving}
              onClick={() => setModalOpen(false)}
            >
              Cancel
            </Button>
          </div>
        </form>
      </Modal>
    </>
  );
}