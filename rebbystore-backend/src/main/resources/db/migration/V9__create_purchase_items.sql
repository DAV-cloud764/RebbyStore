CREATE TABLE purchase_items (
                                id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                purchase_id BIGINT NOT NULL,
                                product_id BIGINT NOT NULL,

                                quantity INTEGER NOT NULL,
                                unit_cost NUMERIC(12,2) NOT NULL,
                                subtotal NUMERIC(14,2) NOT NULL,

                                CONSTRAINT fk_purchase_items_purchase
                                    FOREIGN KEY (purchase_id)
                                        REFERENCES purchases(id)
                                        ON DELETE CASCADE
                                        ON UPDATE RESTRICT,

                                CONSTRAINT fk_purchase_items_product
                                    FOREIGN KEY (product_id)
                                        REFERENCES products(id)
                                        ON DELETE RESTRICT
                                        ON UPDATE RESTRICT,

                                CONSTRAINT chk_purchase_items_quantity
                                    CHECK (quantity > 0),

                                CONSTRAINT chk_purchase_items_unit_cost
                                    CHECK (unit_cost >= 0),

                                CONSTRAINT chk_purchase_items_subtotal
                                    CHECK (subtotal = quantity * unit_cost)
);

CREATE INDEX idx_purchase_items_purchase_id
    ON purchase_items(purchase_id);

CREATE INDEX idx_purchase_items_product_id
    ON purchase_items(product_id);