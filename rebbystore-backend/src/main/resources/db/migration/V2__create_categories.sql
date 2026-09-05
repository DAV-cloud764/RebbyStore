CREATE TABLE categories (
                            id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                            name VARCHAR(100) NOT NULL,
                            slug VARCHAR(120) NOT NULL,
                            description TEXT,

                            is_active BOOLEAN NOT NULL DEFAULT TRUE,

                            created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                            CONSTRAINT uq_categories_name UNIQUE (name),
                            CONSTRAINT uq_categories_slug UNIQUE (slug)
);