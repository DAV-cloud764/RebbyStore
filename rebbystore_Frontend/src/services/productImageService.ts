
import { apiClient } from "./apiClient";

export interface ProductImageRecord {
  id: number;
  imageUrl: string;
  sortOrder: number;
  primary: boolean;
}

export const productImageService = {
  async getByProduct(
    productId: string | number,
  ): Promise<ProductImageRecord[]> {
    return apiClient<ProductImageRecord[]>(
      `/api/products/${Number(productId)}/images`,
      {
        method: "GET",
        authenticated: false,
      },
    );
  },

  async upload(
    productId: string | number,
    file: File,
    sortOrder: number,
    primary: boolean,
  ): Promise<ProductImageRecord> {
    const formData = new FormData();

    formData.append("file", file);
    formData.append("sortOrder", String(sortOrder));
    formData.append("primary", String(primary));

    return apiClient<ProductImageRecord>(
      `/api/products/${Number(productId)}/images/upload`,
      {
        method: "POST",
        authenticated: true,
        body: formData,
      },
    );
  },
};
