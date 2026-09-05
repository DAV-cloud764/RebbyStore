CREATE TABLE product_images (
                                id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                product_id BIGINT NOT NULL,
                                image_url TEXT NOT NULL,
                                sort_order INTEGER NOT NULL DEFAULT 0,
                                is_primary BOOLEAN NOT NULL DEFAULT FALSE,

                                created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT fk_product_images_product
                                    FOREIGN KEY (product_id)
                                        REFERENCES products(id)
                                        ON DELETE CASCADE
                                        ON UPDATE RESTRICT,

                                CONSTRAINT chk_product_images_sort_order
                                    CHECK (sort_order >= 0)
);

CREATE INDEX idx_product_images_product_id
    ON product_images(product_id);