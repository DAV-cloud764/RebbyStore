CREATE TABLE purchases (
                           id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                           supplier_id BIGINT NOT NULL,
                           purchase_number VARCHAR(30) NOT NULL,

                           status VARCHAR(20) NOT NULL DEFAULT 'draft',

                           purchase_date DATE NOT NULL DEFAULT CURRENT_DATE,

                           total_cost NUMERIC(14,2) NOT NULL DEFAULT 0.00,

                           notes TEXT,

                           created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           CONSTRAINT fk_purchases_supplier
                               FOREIGN KEY (supplier_id)
                                   REFERENCES suppliers(id)
                                   ON DELETE RESTRICT
                                   ON UPDATE RESTRICT,

                           CONSTRAINT uq_purchases_purchase_number
                               UNIQUE (purchase_number),

                           CONSTRAINT chk_purchases_status
                               CHECK (
                                   status IN (
                                              'draft',
                                              'ordered',
                                              'received',
                                              'cancelled'
                                       )
                                   ),

                           CONSTRAINT chk_purchases_total_cost
                               CHECK (total_cost >= 0)
);

CREATE INDEX idx_purchases_supplier_id
    ON purchases(supplier_id);

CREATE INDEX idx_purchases_status
    ON purchases(status);