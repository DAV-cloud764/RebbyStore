import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { CartProvider } from './contexts/CartContext';
import { WishlistProvider } from './contexts/WishlistContext';
import { ToastProvider } from './contexts/ToastContext';
import { CustomerLayout } from './components/layout/CustomerLayout';
import { AdminLayout } from './components/layout/AdminLayout';
import Home from './pages/customer/Home';
import Shop from './pages/customer/Shop';
import ProductDetails from './pages/customer/ProductDetails';
import Cart from './pages/customer/Cart';
import Checkout from './pages/customer/Checkout';
import OrderConfirmation from './pages/customer/OrderConfirmation';
import Dashboard from './pages/admin/Dashboard';
import Products from './pages/admin/Products';
import { AddProduct, EditProduct } from './pages/admin/ProductForms';
import Inventory from './pages/admin/Inventory';
import InventoryMovements from './pages/admin/InventoryMovements';
import Purchases from './pages/admin/Purchases';
import Orders from './pages/admin/Orders';
import OrderDetails from './pages/admin/OrderDetails';
import Customers from './pages/admin/Customers';
import Settings from './pages/admin/Settings';
import NotFound from './pages/NotFound';
import Login from "./pages/auth/Login";
import ProtectedRoute from "./components/auth/ProtectedRoute";

export default function App() {
  return (
    <BrowserRouter>
      <ToastProvider>
        <WishlistProvider>
          <CartProvider>
            <Routes>
  <Route element={<CustomerLayout />}>
    <Route path="/" element={<Home />} />
    <Route path="/shop" element={<Shop />} />
    <Route path="/product/:id" element={<ProductDetails />} />
    <Route path="/cart" element={<Cart />} />
    <Route path="/checkout" element={<Checkout />} />
    <Route
      path="/order-confirmation/:id"
      element={<OrderConfirmation />}
    />
    <Route path="*" element={<NotFound />} />
  </Route>

  {/* LOGIN MUST BE OUTSIDE /admin */}
  <Route path="/login" element={<Login />} />

  {/* ADMIN */}
  <Route
    path="/admin"
    element={
      <ProtectedRoute>
        <AdminLayout />
      </ProtectedRoute>
    }
  >
    <Route index element={<Dashboard />} />
    <Route path="products" element={<Products />} />
    <Route path="products/new" element={<AddProduct />} />
    <Route path="products/:id/edit" element={<EditProduct />} />
    <Route path="inventory" element={<Inventory />} />
    <Route
      path="inventory/movements"
      element={<InventoryMovements />}
    />
    <Route path="purchases" element={<Purchases />} />
    <Route path="orders" element={<Orders />} />
    <Route path="orders/:id" element={<OrderDetails />} />
    <Route path="customers" element={<Customers />} />
    <Route path="settings" element={<Settings />} />
  </Route>
</Routes>
          </CartProvider>
        </WishlistProvider>
      </ToastProvider>
    </BrowserRouter>
  );
}
