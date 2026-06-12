CREATE TABLE inventario (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    centro_id           UUID NOT NULL REFERENCES centros(id) ON DELETE CASCADE,
    categoria           VARCHAR(50) NOT NULL,
    stock_actual        INTEGER NOT NULL DEFAULT 0,
    umbral_minimo       INTEGER NOT NULL DEFAULT 10,
    umbral_optimo       INTEGER NOT NULL DEFAULT 50,
    umbral_maximo       INTEGER NOT NULL DEFAULT 200,
    estado_criticidad   VARCHAR(30) NOT NULL DEFAULT 'AGOTADO',
    actualizado_en      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_inventario_centro_categoria UNIQUE (centro_id, categoria)
);

CREATE INDEX idx_inventario_centro_id ON inventario (centro_id);
CREATE INDEX idx_inventario_criticidad ON inventario (estado_criticidad);
