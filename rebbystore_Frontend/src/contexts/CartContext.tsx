import { createContext, useContext, useReducer, useEffect, useCallback } from 'react';
import type { CartItem } from '../types/order';
import type { Product } from '../types/product';
import { getCartTotal, getCartItemCount } from '../utils/helpers';

interface CartState {
  items: CartItem[];
}

type CartAction =
  | { type: 'ADD_ITEM'; product: Product; quantity: number }
  | { type: 'REMOVE_ITEM'; productId: string }
  | { type: 'UPDATE_QUANTITY'; productId: string; quantity: number }
  | { type: 'CLEAR_CART' }
  | { type: 'LOAD_CART'; items: CartItem[] };

function cartReducer(state: CartState, action: CartAction): CartState {
  switch (action.type) {
    case 'LOAD_CART':
      return { items: action.items };

    case 'ADD_ITEM': {
      const existing = state.items.find((i) => i.product.id === action.product.id);
      if (existing) {
        const newQty = Math.min(existing.quantity + action.quantity, action.product.stockQuantity);
        return {
          items: state.items.map((i) =>
            i.product.id === action.product.id ? { ...i, quantity: newQty } : i
          ),
        };
      }
      return {
        items: [...state.items, { product: action.product, quantity: action.quantity }],
      };
    }

    case 'REMOVE_ITEM':
      return { items: state.items.filter((i) => i.product.id !== action.productId) };

    case 'UPDATE_QUANTITY': {
      if (action.quantity <= 0) {
        return { items: state.items.filter((i) => i.product.id !== action.productId) };
      }
      return {
        items: state.items.map((i) =>
          i.product.id === action.productId
            ? { ...i, quantity: Math.min(action.quantity, i.product.stockQuantity) }
            : i
        ),
      };
    }

    case 'CLEAR_CART':
      return { items: [] };

    default:
      return state;
  }
}

interface CartContextType {
  items: CartItem[];
  addItem: (product: Product, quantity?: number) => void;
  removeItem: (productId: string) => void;
  updateQuantity: (productId: string, quantity: number) => void;
  clearCart: () => void;
  total: number;
  itemCount: number;
  isInCart: (productId: string) => boolean;
  getQuantity: (productId: string) => number;
}

const CartContext = createContext<CartContextType | null>(null);
const STORAGE_KEY = 'rebbystore_cart';

export function CartProvider({ children }: { children: React.ReactNode }) {
  const [state, dispatch] = useReducer(cartReducer, { items: [] });

  // Load from localStorage on mount
  useEffect(() => {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (stored) {
        const items: CartItem[] = JSON.parse(stored);
        dispatch({ type: 'LOAD_CART', items });
      }
    } catch {
      // If corrupted, start fresh
    }
  }, []);

  // Persist to localStorage on change
  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state.items));
  }, [state.items]);

  const addItem = useCallback((product: Product, quantity = 1) => {
    dispatch({ type: 'ADD_ITEM', product, quantity });
  }, []);

  const removeItem = useCallback((productId: string) => {
    dispatch({ type: 'REMOVE_ITEM', productId });
  }, []);

  const updateQuantity = useCallback((productId: string, quantity: number) => {
    dispatch({ type: 'UPDATE_QUANTITY', productId, quantity });
  }, []);

  const clearCart = useCallback(() => {
    dispatch({ type: 'CLEAR_CART' });
  }, []);

  const isInCart = useCallback(
    (productId: string) => state.items.some((i) => i.product.id === productId),
    [state.items]
  );

  const getQuantity = useCallback(
    (productId: string) => state.items.find((i) => i.product.id === productId)?.quantity ?? 0,
    [state.items]
  );

  return (
    <CartContext.Provider
      value={{
        items: state.items,
        addItem,
        removeItem,
        updateQuantity,
        clearCart,
        total: getCartTotal(state.items),
        itemCount: getCartItemCount(state.items),
        isInCart,
        getQuantity,
      }}
    >
      {children}
    </CartContext.Provider>
  );
}

export function useCart(): CartContextType {
  const ctx = useContext(CartContext);
  if (!ctx) throw new Error('useCart must be used within CartProvider');
  return ctx;
}
