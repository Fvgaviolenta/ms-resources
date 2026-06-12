CREATE TABLE eventos_procesados (
    evento_id       UUID PRIMARY KEY,
    tipo_evento     VARCHAR(100) NOT NULL,
    procesado_en    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_eventos_procesados_tipo ON eventos_procesados (tipo_evento);
