CREATE TABLE categorias (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo      VARCHAR(50) NOT NULL UNIQUE,
    nombre      VARCHAR(100) NOT NULL,
    activo      BOOLEAN NOT NULL DEFAULT TRUE,
    orden       INTEGER NOT NULL DEFAULT 0,
    creado_en   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_categorias_activo ON categorias (activo);

INSERT INTO categorias (codigo, nombre, orden) VALUES
    ('ALIMENTOS', 'Alimentos', 1),
    ('ARTICULOS_HIGIENE', 'Articulos de higiene', 2),
    ('HERRAMIENTAS', 'Herramientas', 3),
    ('MATERIALES', 'Materiales', 4),
    ('ROPA', 'Ropa', 5),
    ('ARTICULOS_VARIOS', 'Articulos varios', 6);
