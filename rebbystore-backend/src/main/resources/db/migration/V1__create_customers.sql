CREATE TABLE customers (
                           id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                           full_name VARCHAR(150) NOT NULL,
                           phone VARCHAR(10) NOT NULL,
                           email VARCHAR(255) NOT NULL,

                           total_orders INTEGER NOT NULL DEFAULT 0,
                           total_spent NUMERIC(14, 2) NOT NULL DEFAULT 0.00,

                           created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           CONSTRAINT uq_customers_phone
                               UNIQUE (phone),

                           CONSTRAINT chk_customers_phone
                               CHECK (phone ~ '^(06|07)[0-9]{8}$'),

    CONSTRAINT chk_customers_total_orders
        CHECK (total_orders >= 0),

    CONSTRAINT chk_customers_total_spent
        CHECK (total_spent >= 0)
);