import { useEffect, useState } from 'react';
import { useOutletContext, Link } from 'react-router-dom';
import {
  Package,
  Layers,
  ShoppingCart,
  DollarSign,
  AlertTriangle,
  XCircle,
  ArrowRightLeft,
} from 'lucide-react';

import { AdminHeader } from '../../components/admin/AdminSidebar';
import { StatCard } from '../../components/admin/StatCard';
import {
  Badge,
  orderStatusBadgeVariant,
  orderStatusLabel,
} from '../../components/ui/Badge';

import { productService } from '../../services/productService';
import { orderService } from '../../services/orderService';
import { reportingService } from '../../services/reportingService';

import { getStockStatus } from '../../types/product';
import type { Product } from '../../types/product';
import type { Order } from '../../types/order';

import type {
  InventorySummary,
  SalesSummary,
  PurchaseSummary,
} from '../../services/reportingService';

import { formatPrice, formatDate } from '../../utils/formatting';

interface OutletCtx {
  onMenuClick: () => void;
}

export default function Dashboard() {
  const { onMenuClick } =
    useOutletContext<OutletCtx>();

  const [products, setProducts] =
    useState<Product[]>([]);

  const [orders, setOrders] =
    useState<Order[]>([]);

  const [inventorySummary, setInventorySummary] =
    useState<InventorySummary | null>(null);

  const [salesSummary, setSalesSummary] =
    useState<SalesSummary | null>(null);

  const [purchaseSummary, setPurchaseSummary] =
    useState<PurchaseSummary | null>(null);

  useEffect(() => {
    async function loadDashboard() {
      const [
        productsResult,
        ordersResult,
        inventoryResult,
        salesResult,
        purchaseResult,
      ] = await Promise.all([
        productService.getAll(),
        orderService.getAll(),
        reportingService.getInventorySummary(),
        reportingService.getSalesSummary(),
        reportingService.getPurchaseSummary(),
      ]);

      setProducts(productsResult);
      setOrders(ordersResult);
      setInventorySummary(inventoryResult);
      setSalesSummary(salesResult);
      setPurchaseSummary(purchaseResult);
    }

    void loadDashboard();
  }, []);

  const outOfStock = products.filter(
    (product) =>
      getStockStatus(product) === 'out-of-stock',
  );

  const lowStockProducts = products.filter(
    (product) =>
      getStockStatus(product) === 'low-stock',
  );

  const recentOrders = orders.slice(0, 5);

  return (
    <>
      <AdminHeader
        title="Dashboard"
        onMenuClick={onMenuClick}
      />

      <main className="p-4 md:p-6 space-y-8">

        {/* KPI Summary */}
        <section>
          <div className="grid grid-cols-2 xl:grid-cols-4 gap-3">
            <StatCard
              label="Total Products"
              value={
                inventorySummary?.totalProducts ?? 0
              }
              sub="Products in catalog"
              icon={<Package size={18} />}
            />

            <StatCard
              label="Current Stock"
              value={
                inventorySummary?.totalUnitsInStock ?? 0
              }
              sub="Total units"
              icon={<Layers size={18} />}
            />

            <StatCard
              label="Pending Orders"
              value={
                salesSummary
                  ? orders.filter(
                      (order) =>
                        order.status === 'pending',
                    ).length
                  : 0
              }
              sub="Awaiting processing"
              icon={<ShoppingCart size={18} />}
            />

            <StatCard
              label="Sales Revenue"
              value={formatPrice(
                salesSummary?.totalRevenue ?? 0,
              )}
              sub="From delivered orders"
              icon={<DollarSign size={18} />}
              accent
            />
          </div>
        </section>

        {/* Inventory Operations */}
        <section>
          <div className="bg-white border border-rs-border p-5 md:p-6">
            <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
              <div>
                <p className="text-xs uppercase tracking-[0.14em] text-rs-muted">
                  Inventory Operations
                </p>

                <h2 className="mt-1 font-display text-lg font-semibold text-rs-ink">
                  Record a stock movement
                </h2>

                <p className="mt-1 text-sm text-rs-muted">
                  Add or remove stock using the inventory
                  workflow.
                </p>
              </div>

              <div className="flex gap-2">
                <Link
                  to="/admin/inventory"
                  className="inline-flex items-center justify-center gap-2 bg-rs-accent px-4 py-2.5 text-sm font-medium text-rs-ink hover:opacity-90 transition-opacity"
                >
                  <ArrowRightLeft size={16} />
                  Record Movement
                </Link>

                <Link
                  to="/admin/inventory/movements"
                  className="inline-flex items-center justify-center px-4 py-2.5 text-sm text-rs-muted border border-rs-border hover:text-rs-ink hover:bg-rs-surface transition-colors"
                >
                  View History
                </Link>
              </div>
            </div>
          </div>
        </section>

        {/* Operational Summary */}
        <section>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">

            <div className="bg-white border border-rs-border p-5">
              <p className="text-xs text-rs-muted uppercase tracking-wide">
                Low Stock
              </p>

              <p className="mt-1 text-2xl font-display font-bold text-rs-ink">
                {inventorySummary?.lowStockProducts ?? 0}
              </p>

              <p className="text-xs text-rs-muted">
                Products at or below threshold
              </p>
            </div>

            <div className="bg-white border border-rs-border p-5">
              <p className="text-xs text-rs-muted uppercase tracking-wide">
                Delivered Orders
              </p>

              <p className="mt-1 text-2xl font-display font-bold text-rs-ink">
                {salesSummary?.deliveredCount ?? 0}
              </p>

              <p className="text-xs text-rs-muted">
                Completed sales
              </p>
            </div>

            <div className="bg-white border border-rs-border p-5">
              <p className="text-xs text-rs-muted uppercase tracking-wide">
                Purchase Spend
              </p>

              <p className="mt-1 text-xl font-display font-bold text-rs-ink">
                {formatPrice(
                  purchaseSummary?.totalPurchaseCost ?? 0,
                )}
              </p>

              <p className="text-xs text-rs-muted">
                Received purchases
              </p>
            </div>
          </div>
        </section>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">

          {/* Recent Orders */}
          <section className="lg:col-span-2 bg-white border border-rs-border">
            <div className="flex items-center justify-between px-5 py-4 border-b border-rs-border">
              <h2 className="font-display font-semibold text-rs-ink">
                Recent Orders
              </h2>

              <Link
                to="/admin/orders"
                className="text-xs text-rs-muted hover:text-rs-ink transition-colors"
              >
                View all →
              </Link>
            </div>

            <div className="overflow-x-auto">
              <table className="admin-table">
                <thead>
                  <tr>
                    <th>Order #</th>
                    <th>Customer</th>
                    <th>Total</th>
                    <th>Date</th>
                    <th>Status</th>
                  </tr>
                </thead>

                <tbody>
                  {recentOrders.map((order) => (
                    <tr key={order.id}>
                      <td>
                        <Link
                          to={`/admin/orders/${order.id}`}
                          className="font-mono text-xs text-rs-ink hover:text-rs-accent"
                        >
                          {order.orderNumber}
                        </Link>
                      </td>

                      <td className="text-xs">
                        {order.customer.fullName}
                      </td>

                      <td className="font-medium text-xs">
                        {formatPrice(order.total)}
                      </td>

                      <td className="text-xs text-rs-muted">
                        {formatDate(order.createdAt)}
                      </td>

                      <td>
                        <Badge
                          variant={orderStatusBadgeVariant(
                            order.status,
                          )}
                        >
                          {orderStatusLabel(order.status)}
                        </Badge>
                      </td>
                    </tr>
                  ))}

                  {recentOrders.length === 0 && (
                    <tr>
                      <td
                        colSpan={5}
                        className="text-center py-8 text-sm text-rs-muted"
                      >
                        No orders found.
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </section>

          {/* Stock Alerts */}
          <section className="bg-white border border-rs-border">
            <div className="flex items-center justify-between px-5 py-4 border-b border-rs-border">
              <h2 className="font-display font-semibold text-rs-ink">
                Stock Alerts
              </h2>

              <Link
                to="/admin/inventory"
                className="text-xs text-rs-muted hover:text-rs-ink transition-colors"
              >
                View all →
              </Link>
            </div>

            <div className="p-4 space-y-3">

              {outOfStock.map((product) => (
                <div
                  key={product.id}
                  className="flex items-center gap-3 p-3 bg-red-50 border border-red-100"
                >
                  <XCircle
                    size={15}
                    className="text-red-500 shrink-0"
                  />

                  <div className="min-w-0">
                    <p className="text-xs font-semibold text-rs-ink truncate">
                      {product.name}
                    </p>

                    <p className="text-[11px] text-red-500">
                      Out of stock
                    </p>
                  </div>
                </div>
              ))}

              {lowStockProducts.map((product) => (
                <div
                  key={product.id}
                  className="flex items-center gap-3 p-3 bg-amber-50 border border-amber-100"
                >
                  <AlertTriangle
                    size={15}
                    className="text-amber-600 shrink-0"
                  />

                  <div className="min-w-0">
                    <p className="text-xs font-semibold text-rs-ink truncate">
                      {product.name}
                    </p>

                    <p className="text-[11px] text-amber-600">
                      Only {product.stockQuantity} left
                    </p>
                  </div>
                </div>
              ))}

              {outOfStock.length === 0 &&
                lowStockProducts.length === 0 && (
                  <p className="text-xs text-rs-muted text-center py-4">
                    All stock levels are healthy.
                  </p>
                )}
            </div>
          </section>
        </div>
      </main>
    </>
  );
}