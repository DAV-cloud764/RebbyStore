import { useEffect, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import type { FormEvent } from 'react';
import { ArrowDown, ArrowUp } from 'lucide-react';

import { AdminHeader } from '../../components/admin/AdminSidebar';
import { InventoryTable } from '../../components/admin/InventoryTable';
import { Modal } from '../../components/ui/Modal';
import { Button } from '../../components/ui/Button';
import { Select } from '../../components/ui/Select';
import { Input } from '../../components/ui/Input';

import { productService } from '../../services/productService';
import { inventoryService } from '../../services/inventoryService';
import { ApiError } from '../../services/apiClient';

import type { Product } from '../../types/product';
import { useToast } from '../../contexts/ToastContext';

interface OutletCtx {
  onMenuClick: () => void;
}

type MovementModal = 'none' | 'in' | 'out';

export default function Inventory() {
  const { onMenuClick } =
    useOutletContext<OutletCtx>();

  const { showToast } = useToast();

  const [products, setProducts] =
    useState<Product[]>([]);

  const [modal, setModal] =
    useState<MovementModal>('none');

  const [saving, setSaving] =
    useState(false);

  const [form, setForm] = useState({
    productId: '',
    quantity: '',
  });

  useEffect(() => {
    async function loadProducts() {
      try {
        const result =
          await productService.getAll();

        setProducts(result);
      } catch (error) {
        const message =
          error instanceof ApiError
            ? error.message
            : 'Failed to load inventory';

        showToast(message, 'error');
      }
    }

    void loadProducts();
  }, [showToast]);

  function handleChange(
    key: 'productId' | 'quantity',
    value: string,
  ) {
    setForm((current) => ({
      ...current,
      [key]: value,
    }));
  }

  function openModal(type: MovementModal) {
    setForm({
      productId: '',
      quantity: '',
    });

    setModal(type);
  }

  function closeModal() {
    if (!saving) {
      setModal('none');
    }
  }

  async function handleSubmit(
    event: FormEvent,
  ) {
    event.preventDefault();

    if (!form.productId) {
      showToast(
        'Product is required',
        'error',
      );
      return;
    }

    const quantity =
      Number(form.quantity);

    if (!Number.isInteger(quantity) || quantity <= 0) {
      showToast(
        'Quantity must be a whole number greater than zero',
        'error',
      );
      return;
    }

    const product = products.find(
      (item) => item.id === form.productId,
    );

    if (!product) {
      showToast(
        'Selected product could not be found',
        'error',
      );
      return;
    }

    if (
      modal === 'out' &&
      quantity > product.stockQuantity
    ) {
      showToast(
        'Quantity exceeds current stock',
        'error',
      );
      return;
    }

    setSaving(true);

    try {
      if (modal === 'in') {
        await inventoryService.stockIn(
          product.id,
          quantity,
        );
      } else if (modal === 'out') {
        await inventoryService.stockOut(
          product.id,
          quantity,
        );
      }

      const updated =
        await productService.getAll();

      setProducts(updated);
      setModal('none');

      showToast(
        `Stock ${
          modal === 'in'
            ? 'added'
            : 'removed'
        } successfully`,
      );
    } catch (error) {
      const message =
        error instanceof ApiError
          ? error.message
          : 'Failed to update inventory';

      showToast(message, 'error');
    } finally {
      setSaving(false);
    }
  }

  const selectedProduct =
    products.find(
      (product) =>
        product.id === form.productId,
    );

  const productOptions =
    products.map((product) => ({
      value: product.id,
      label: `${product.name} (${product.sku})`,
    }));

  const quantity =
    Number(form.quantity || 0);

  return (
    <>
      <AdminHeader
        title="Inventory — Stock Overview"
        onMenuClick={onMenuClick}
        actions={
          <div className="flex gap-2">
            <Button
              variant="secondary"
              size="sm"
              onClick={() =>
                openModal('out')
              }
            >
              <ArrowDown size={14} />
              Stock Out
            </Button>

            <Button
              size="sm"
              onClick={() =>
                openModal('in')
              }
            >
              <ArrowUp size={14} />
              Stock In
            </Button>
          </div>
        }
      />

      <main className="p-4 md:p-6">
        <div className="bg-white border border-rs-border overflow-hidden">
          <InventoryTable
            products={products}
          />
        </div>

        <p className="text-xs text-rs-muted mt-3">
          {products.length} products
        </p>
      </main>

      <Modal
        isOpen={modal === 'in'}
        onClose={closeModal}
        title="Record Stock In"
        size="md"
      >
        <form
          onSubmit={handleSubmit}
          className="space-y-4"
        >
          <Select
            label="Product"
            options={productOptions}
            placeholder="Select product…"
            value={form.productId}
            onChange={(event) =>
              handleChange(
                'productId',
                event.target.value,
              )
            }
            required
          />

          {selectedProduct && (
            <div className="px-3 py-2 bg-rs-surface text-xs text-rs-muted border border-rs-border">
              Current stock:{' '}
              <strong className="text-rs-ink">
                {selectedProduct.stockQuantity}
              </strong>{' '}
              units
            </div>
          )}

          <Input
            label="Quantity Received"
            type="number"
            min={1}
            step={1}
            value={form.quantity}
            onChange={(event) =>
              handleChange(
                'quantity',
                event.target.value,
              )
            }
            required
            placeholder="10"
          />

          <div className="px-3 py-2.5 bg-green-50 border border-green-200 text-sm text-green-700">
            {selectedProduct && quantity > 0 ? (
              <>
                Stock will update:{' '}
                {selectedProduct.stockQuantity}
                {' → '}
                <strong>
                  {selectedProduct.stockQuantity +
                    quantity}
                </strong>
              </>
            ) : (
              'Select a product and enter a quantity.'
            )}
          </div>

          <div className="px-3 py-2.5 bg-rs-surface border border-rs-border text-xs text-rs-muted">
            Manual stock-in is recorded by the
            backend as an inventory adjustment.
          </div>

          <div className="flex gap-3 pt-2">
            <Button
              type="submit"
              loading={saving}
              className="flex-1 justify-center"
            >
              Confirm Stock In
            </Button>

            <Button
              variant="secondary"
              type="button"
              disabled={saving}
              onClick={closeModal}
            >
              Cancel
            </Button>
          </div>
        </form>
      </Modal>

      <Modal
        isOpen={modal === 'out'}
        onClose={closeModal}
        title="Record Stock Out"
        size="md"
      >
        <form
          onSubmit={handleSubmit}
          className="space-y-4"
        >
          <Select
            label="Product"
            options={productOptions}
            placeholder="Select product…"
            value={form.productId}
            onChange={(event) =>
              handleChange(
                'productId',
                event.target.value,
              )
            }
            required
          />

          {selectedProduct && (
            <div className="px-3 py-2 bg-rs-surface text-xs text-rs-muted border border-rs-border">
              Current stock:{' '}
              <strong className="text-rs-ink">
                {selectedProduct.stockQuantity}
              </strong>{' '}
              units
            </div>
          )}

          <Input
            label="Quantity"
            type="number"
            min={1}
            max={
              selectedProduct?.stockQuantity
            }
            step={1}
            value={form.quantity}
            onChange={(event) =>
              handleChange(
                'quantity',
                event.target.value,
              )
            }
            required
            placeholder="2"
          />

          {selectedProduct && quantity > 0 && (
            <div
              className={`px-3 py-2.5 border text-sm ${
                quantity >
                selectedProduct.stockQuantity
                  ? 'bg-red-50 border-red-200 text-red-700'
                  : 'bg-amber-50 border-amber-200 text-amber-700'
              }`}
            >
              {quantity >
              selectedProduct.stockQuantity ? (
                'Quantity exceeds current stock'
              ) : (
                <>
                  Stock will update:{' '}
                  {selectedProduct.stockQuantity}
                  {' → '}
                  <strong>
                    {selectedProduct.stockQuantity -
                      quantity}
                  </strong>
                </>
              )}
            </div>
          )}

          <div className="px-3 py-2.5 bg-rs-surface border border-rs-border text-xs text-rs-muted">
            Manual stock-out is recorded by the
            backend as an inventory adjustment.
          </div>

          <div className="flex gap-3 pt-2">
            <Button
              type="submit"
              loading={saving}
              disabled={
                !!(
                  selectedProduct &&
                  quantity >
                    selectedProduct.stockQuantity
                )
              }
              className="flex-1 justify-center"
            >
              Confirm Stock Out
            </Button>

            <Button
              variant="secondary"
              type="button"
              disabled={saving}
              onClick={closeModal}
            >
              Cancel
            </Button>
          </div>
        </form>
      </Modal>
    </>
  );
}