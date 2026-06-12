CREATE TABLE centros (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre                  VARCHAR(200) NOT NULL,
    direccion               VARCHAR(500),
    coordenadas             geography(Point, 4326) NOT NULL,
    region                  VARCHAR(100),
    comuna                  VARCHAR(100),
    capacidad               INTEGER,
    horario                 VARCHAR(200),
    estado                  VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    emergencia_id           UUID,
    creado_por_usuario_id   UUID,
    creado_en               TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    actualizado_en          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_centros_coordenadas ON centros USING GIST (coordenadas);
CREATE INDEX idx_centros_emergencia_id ON centros (emergencia_id);
CREATE INDEX idx_centros_estado ON centros (estado);
