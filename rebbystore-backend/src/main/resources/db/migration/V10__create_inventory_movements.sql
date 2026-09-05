CREATE TABLE inventory_movements (
                                     id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                     product_id BIGINT NOT NULL,
                                     order_id BIGINT,
                                     purchase_id BIGINT,

                                     type VARCHAR(10) NOT NULL,
                                     quantity INTEGER NOT NULL,
                                     reason VARCHAR(30) NOT NULL,

                                     created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                     CONSTRAINT fk_inventory_movements_product
                                         FOREIGN KEY (product_id)
                                             REFERENCES products(id)
                                             ON DELETE RESTRICT
                                             ON UPDATE RESTRICT,

                                     CONSTRAINT fk_inventory_movements_order
                                         FOREIGN KEY (order_id)
                                             REFERENCES orders(id)
                                             ON DELETE RESTRICT
                                             ON UPDATE RESTRICT,

                                     CONSTRAINT fk_inventory_movements_purchase
                                         FOREIGN KEY (purchase_id)
                                             REFERENCES purchases(id)
                                             ON DELETE RESTRICT
                                             ON UPDATE RESTRICT,

                                     CONSTRAINT chk_inventory_movement_type
                                         CHECK (type IN ('in', 'out')),

                                     CONSTRAINT chk_inventory_movement_quantity
                                         CHECK (quantity > 0),

                                     CONSTRAINT chk_inventory_movement_reason
                                         CHECK (
                                             reason IN (
                                                        'purchase',
                                                        'sale',
                                                        'adjustment',
                                                        'damaged',
                                                        'lost',
                                                        'returned_to_supplier'
                                                 )
                                             ),

                                     CONSTRAINT chk_inventory_movement_reference
                                         CHECK (
                                             (reason = 'sale'
                                                 AND type = 'out'
                                                 AND order_id IS NOT NULL
                                                 AND purchase_id IS NULL)

                                                 OR

                                             (reason = 'purchase'
                                                 AND type = 'in'
                                                 AND purchase_id IS NOT NULL
                                                 AND order_id IS NULL)

                                                 OR

                                             (reason = 'adjustment'
                                                 AND order_id IS NULL
                                                 AND purchase_id IS NULL)

                                                 OR

                                             (reason = 'damaged'
                                                 AND type = 'out'
                                                 AND order_id IS NULL
                                                 AND purchase_id IS NULL)

                                                 OR

                                             (reason = 'lost'
                                                 AND type = 'out'
                                                 AND order_id IS NULL
                                                 AND purchase_id IS NULL)

                                                 OR

                                             (reason = 'returned_to_supplier'
                                                 AND type = 'out'
                                                 AND purchase_id IS NOT NULL
                                                 AND order_id IS NULL)
                                             )
);

CREATE INDEX idx_inventory_movements_product_id
    ON inventory_movements(product_id);

CREATE INDEX idx_inventory_movements_order_id
    ON inventory_movements(order_id);

CREATE INDEX idx_inventory_movements_purchase_id
    ON inventory_movements(purchase_id);

CREATE INDEX idx_inventory_movements_created_at
    ON inventory_movements(created_at);