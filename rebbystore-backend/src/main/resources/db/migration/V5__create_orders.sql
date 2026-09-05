CREATE TABLE orders (
                        id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                        customer_id BIGINT NOT NULL,
                        order_number VARCHAR(30) NOT NULL,

                        customer_name VARCHAR(150) NOT NULL,
                        customer_phone VARCHAR(10) NOT NULL,
                        customer_email VARCHAR(255) NOT NULL,

                        delivery_address VARCHAR(255) NOT NULL,
                        delivery_city VARCHAR(100) NOT NULL,
                        delivery_region VARCHAR(100) NOT NULL,
                        delivery_notes TEXT,

                        payment_method VARCHAR(30) NOT NULL DEFAULT 'cash_on_delivery',

                        subtotal NUMERIC(14,2) NOT NULL,
                        delivery_fee NUMERIC(12,2) NOT NULL,
                        total NUMERIC(14,2) NOT NULL,

                        status VARCHAR(30) NOT NULL DEFAULT 'pending',

                        expires_at TIMESTAMPTZ,

                        created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                        CONSTRAINT fk_orders_customer
                            FOREIGN KEY (customer_id)
                                REFERENCES customers(id)
                                ON DELETE RESTRICT
                                ON UPDATE RESTRICT,

                        CONSTRAINT uq_orders_order_number
                            UNIQUE (order_number),

                        CONSTRAINT chk_orders_payment_method
                            CHECK (payment_method = 'cash_on_delivery'),

                        CONSTRAINT chk_orders_subtotal
                            CHECK (subtotal >= 0),

                        CONSTRAINT chk_orders_delivery_fee
                            CHECK (delivery_fee >= 0),

                        CONSTRAINT chk_orders_total
                            CHECK (total >= 0),

                        CONSTRAINT chk_orders_total_consistency
                            CHECK (total = subtotal + delivery_fee),

                        CONSTRAINT chk_orders_status
                            CHECK (
                                status IN (
                                           'pending',
                                           'confirmed',
                                           'processing',
                                           'ready_for_delivery',
                                           'delivered',
                                           'cancelled'
                                    )
                                ),

                        CONSTRAINT chk_orders_phone
                            CHECK (customer_phone ~ '^(06|07)[0-9]{8}$'),

    CONSTRAINT chk_orders_expiry
        CHECK (status <> 'pending' OR expires_at IS NOT NULL)
);

CREATE INDEX idx_orders_customer_id
    ON orders(customer_id);

CREATE INDEX idx_orders_status
    ON orders(status);

CREATE INDEX idx_orders_created_at
    ON orders(created_at);

CREATE INDEX idx_orders_pending_expiry
    ON orders(expires_at)
    WHERE status = 'pending';