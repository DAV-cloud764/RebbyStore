import type { Product } from "../types/product";
import { ApiError, apiClient } from "./apiClient";
import heroImage from "../assets/hero.png";

interface BackendProduct {
  id: number;
  name: string;
  sku: string;
  description: string;
  price: number;
  categoryId: number;
  color: string;
  texture: string;
  length: string;
  hairType: string;
  stockQuantity: number;
  lowStockThreshold: number;
  status: "ACTIVE" | "INACTIVE";
}

interface BackendCategory {
  id: number;
  name: string;
  slug: string;
}

interface BackendProductImage {
  id: number;
  imageUrl: string;
  sortOrder: number;
  primary: boolean;
}

interface ProductWritePayload {
  name: string;
  sku: string;
  description: string;
  price: number;
  categoryId: number;
  color: string;
  texture: string;
  length: string;
  hairType: string;
  lowStockThreshold: number;
}

let categoriesCache: BackendCategory[] | null = null;

async function getCategories(): Promise<BackendCategory[]> {
  if (categoriesCache) {
    return categoriesCache;
  }

  const response = await apiClient<BackendCategory[]>(
    "/api/categories",
    {
      method: "GET",
      authenticated: false,
    },
  );

  categoriesCache = response;

  return response;
}

function normalizeStatus(
  status: BackendProduct["status"],
): Product["status"] {
  return status.toLowerCase() as Product["status"];
}

function normalizeTexture(value: string): Product["texture"] {
  const normalized = value
    .trim()
    .toLowerCase()
    .replace(/\s+/g, "-");

  return normalized as Product["texture"];
}

function normalizeHairType(value: string): Product["hairType"] {
  const normalized = value.trim().toLowerCase();

  if (normalized.includes("synthetic")) {
    return "synthetic";
  }

  if (normalized.includes("blend")) {
    return "blend";
  }

  return "human";
}

function normalizeCategory(
  categorySlug: string | undefined,
): Product["category"] {
  const allowedCategories: Product["category"][] = [
    "human-hair",
    "synthetic",
    "lace-front",
    "closure",
    "bob",
  ];

  if (
    categorySlug &&
    allowedCategories.includes(
      categorySlug as Product["category"],
    )
  ) {
    return categorySlug as Product["category"];
  }

  return "human-hair";
}

async function getProductImages(
  productId: number,
): Promise<string[]> {
  const response = await apiClient<BackendProductImage[]>(
    `/api/products/${productId}/images`,
    {
      method: "GET",
      authenticated: false,
    },
  );

  return response
    .sort((a, b) => a.sortOrder - b.sortOrder)
    .map((image) => image.imageUrl);
}

async function mapProduct(
  backendProduct: BackendProduct,
): Promise<Product> {
  const [categories, productImages] = await Promise.all([
    getCategories(),
    getProductImages(backendProduct.id),
  ]);

  const category = categories.find(
    (item) => item.id === backendProduct.categoryId,
  );

  return {
    id: String(backendProduct.id),
    name: backendProduct.name,
    sku: backendProduct.sku,
    price: backendProduct.price,
    images: productImages.length > 0
      ? productImages
      : [heroImage],
    description: backendProduct.description,
    category: normalizeCategory(category?.slug),
    color: backendProduct.color,
    texture: normalizeTexture(backendProduct.texture),
    length: backendProduct.length,
    hairType: normalizeHairType(backendProduct.hairType),
    stockQuantity: backendProduct.stockQuantity,
    lowStockThreshold: backendProduct.lowStockThreshold,
    status: normalizeStatus(backendProduct.status),

    // These fields do not currently exist in the backend product model.
    isNew: false,
    isFeatured: false,
    originalPrice: undefined,
  };
}

async function mapProducts(
  backendProducts: BackendProduct[],
): Promise<Product[]> {
  return Promise.all(
    backendProducts.map((product) => mapProduct(product)),
  );
}

async function getCategoryId(
  categorySlug: Product["category"],
): Promise<number> {
  const categories = await getCategories();

  const category = categories.find(
    (item) => item.slug === categorySlug,
  );

  if (!category) {
    throw new Error(
      `Product category '${categorySlug}' was not found.`,
    );
  }

  return category.id;
}

function toWritePayload(
  product: Omit<Product, "id">,
  categoryId: number,
): ProductWritePayload {
  return {
    name: product.name,
    sku: product.sku,
    description: product.description,
    price: product.price,
    categoryId,
    color: product.color,
    texture: product.texture,
    length: product.length,
    hairType: product.hairType,
    lowStockThreshold: product.lowStockThreshold,
  };
}

export const productService = {
  async getAll(): Promise<Product[]> {
    const response = await apiClient<BackendProduct[]>(
      "/api/products",
      {
        method: "GET",
        authenticated: false,
      },
    );

    return mapProducts(response);
  },

  async getById(id: string): Promise<Product | undefined> {
    try {
      const response = await apiClient<BackendProduct>(
        `/api/products/${id}`,
        {
          method: "GET",
          authenticated: false,
        },
      );

      return mapProduct(response);
    } catch (error) {
      if (error instanceof ApiError && error.status === 404) {
        return undefined;
      }

      throw error;
    }
  },

  async getFeatured(): Promise<Product[]> {
    const products = await this.getAll();

    return products.filter(
      (product) =>
        product.isFeatured &&
        product.status === "active",
    );
  },

  async getNewArrivals(): Promise<Product[]> {
    const products = await this.getAll();

    return products.filter(
      (product) =>
        product.isNew &&
        product.status === "active",
    );
  },

  async create(
    data: Omit<Product, "id">,
  ): Promise<Product> {
    const categoryId = await getCategoryId(data.category);

    const payload = toWritePayload(data, categoryId);

    const response = await apiClient<BackendProduct>(
      "/api/products",
      {
        method: "POST",
        authenticated: true,
        body: JSON.stringify(payload),
      },
    );

    return mapProduct(response);
  },

  async update(
    id: string,
    data: Partial<Product>,
  ): Promise<Product> {
    const existing = await this.getById(id);

    if (!existing) {
      throw new ApiError(
        404,
        `Product with ID '${id}' was not found.`,
      );
    }

    const merged: Product = {
      ...existing,
      ...data,
    };

    const categoryId = await getCategoryId(
      merged.category,
    );

    const payload = toWritePayload(
      merged,
      categoryId,
    );

    const response = await apiClient<BackendProduct>(
      `/api/products/${id}`,
      {
        method: "PUT",
        authenticated: true,
        body: JSON.stringify(payload),
      },
    );

    return mapProduct(response);
  },

  async delete(id: string): Promise<void> {
    await apiClient<void>(
      `/api/products/${id}`,
      {
        method: "DELETE",
        authenticated: true,
      },
    );
  },
};