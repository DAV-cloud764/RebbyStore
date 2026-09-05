CREATE TABLE order_items (
                             id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                             order_id BIGINT NOT NULL,
                             product_id BIGINT NOT NULL,

                             product_name VARCHAR(200) NOT NULL,
                             sku VARCHAR(50) NOT NULL,

                             quantity INTEGER NOT NULL,
                             unit_price NUMERIC(12,2) NOT NULL,
                             subtotal NUMERIC(14,2) NOT NULL,

                             CONSTRAINT fk_order_items_order
                                 FOREIGN KEY (order_id)
                                     REFERENCES orders(id)
                                     ON DELETE CASCADE
                                     ON UPDATE RESTRICT,

                             CONSTRAINT fk_order_items_product
                                 FOREIGN KEY (product_id)
                                     REFERENCES products(id)
                                     ON DELETE RESTRICT
                                     ON UPDATE RESTRICT,

                             CONSTRAINT chk_order_items_quantity
                                 CHECK (quantity > 0),

                             CONSTRAINT chk_order_items_unit_price
                                 CHECK (unit_price >= 0),

                             CONSTRAINT chk_order_items_subtotal
                                 CHECK (subtotal = quantity * unit_price)
);

CREATE INDEX idx_order_items_order_id
    ON order_items(order_id);

CREATE INDEX idx_order_items_product_id
    ON order_items(product_id);