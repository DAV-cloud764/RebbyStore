ALTER TABLE inventory_movements
DROP CONSTRAINT chk_inventory_movement_reference;

ALTER TABLE inventory_movements
    ADD CONSTRAINT chk_inventory_movement_reference
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

            (reason = 'adjustment'
                AND type = 'in'
                AND order_id IS NOT NULL
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
            );