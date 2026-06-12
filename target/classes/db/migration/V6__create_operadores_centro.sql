CREATE TABLE operadores_centro (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    centro_id               UUID NOT NULL REFERENCES centros(id) ON DELETE CASCADE,
    usuario_id              UUID NOT NULL,
    asignado_por_usuario_id UUID NOT NULL,
    asignado_en             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_operador_centro UNIQUE (centro_id, usuario_id)
);

CREATE INDEX idx_operadores_centro_centro_id ON operadores_centro (centro_id);
CREATE INDEX idx_operadores_centro_usuario_id ON operadores_centro (usuario_id);
