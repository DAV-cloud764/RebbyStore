CREATE TABLE suppliers (
                           id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                           name VARCHAR(150) NOT NULL,
                           phone VARCHAR(10),
                           email VARCHAR(255),
                           address VARCHAR(255),

                           created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);