CREATE TABLE movimientos_inventario (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inventario_id               UUID NOT NULL REFERENCES inventario(id),
    centro_id                   UUID NOT NULL REFERENCES centros(id),
    categoria                   VARCHAR(50) NOT NULL,
    tipo_movimiento             VARCHAR(20) NOT NULL,
    cantidad                    INTEGER NOT NULL,
    stock_anterior              INTEGER NOT NULL,
    stock_posterior             INTEGER NOT NULL,
    estado_criticidad_posterior VARCHAR(30) NOT NULL,
    registrado_por_usuario_id   UUID,
    registrado_en               TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_movimientos_inventario_centro_id ON movimientos_inventario (centro_id);
CREATE INDEX idx_movimientos_inventario_inventario_id ON movimientos_inventario (inventario_id);
CREATE INDEX idx_movimientos_inventario_registrado_en ON movimientos_inventario (registrado_en DESC);
