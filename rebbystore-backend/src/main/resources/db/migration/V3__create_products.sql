CREATE TABLE products (
                          id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                          category_id BIGINT NOT NULL,
                          name VARCHAR(200) NOT NULL,
                          sku VARCHAR(50) NOT NULL,
                          description TEXT,

                          price NUMERIC(12,2) NOT NULL,

                          color VARCHAR(100),
                          texture VARCHAR(100),
                          length VARCHAR(50),
                          hair_type VARCHAR(100),

                          stock_quantity INTEGER NOT NULL DEFAULT 0,
                          low_stock_threshold INTEGER NOT NULL DEFAULT 5,

                          status VARCHAR(20) NOT NULL DEFAULT 'active',

                          created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT fk_products_category
                              FOREIGN KEY (category_id)
                                  REFERENCES categories(id)
                                  ON DELETE RESTRICT
                                  ON UPDATE RESTRICT,

                          CONSTRAINT uq_products_sku
                              UNIQUE (sku),

                          CONSTRAINT chk_products_price
                              CHECK (price >= 0),

                          CONSTRAINT chk_products_stock
                              CHECK (stock_quantity >= 0),

                          CONSTRAINT chk_products_low_stock_threshold
                              CHECK (low_stock_threshold >= 0),

                          CONSTRAINT chk_products_status
                              CHECK (status IN ('active', 'inactive'))
);

CREATE INDEX idx_products_category_id
    ON products(category_id);

CREATE INDEX idx_products_status
    ON products(status);