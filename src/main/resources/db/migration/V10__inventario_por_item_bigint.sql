ALTER TABLE inventario ADD COLUMN item_catalogo_id UUID REFERENCES catalogo_items(id);

UPDATE inventario i
SET item_catalogo_id = (
    SELECT ci.id
    FROM catalogo_items ci
    INNER JOIN categorias cat ON cat.id = ci.categoria_id
    WHERE cat.codigo = i.categoria
    ORDER BY ci.nombre
    LIMIT 1
);

INSERT INTO inventario (centro_id, item_catalogo_id, stock_actual, umbral_minimo, umbral_optimo, umbral_maximo, estado_criticidad, actualizado_en)
SELECT c.id, ci.id, 0, 10, 50, 200, 'AGOTADO', NOW()
FROM centros c
CROSS JOIN catalogo_items ci
WHERE ci.activo = TRUE
  AND NOT EXISTS (
      SELECT 1 FROM inventario i2
      WHERE i2.centro_id = c.id AND i2.item_catalogo_id = ci.id
  );

ALTER TABLE inventario ALTER COLUMN stock_actual TYPE BIGINT;
ALTER TABLE inventario ALTER COLUMN umbral_minimo TYPE BIGINT;
ALTER TABLE inventario ALTER COLUMN umbral_optimo TYPE BIGINT;
ALTER TABLE inventario ALTER COLUMN umbral_maximo TYPE BIGINT;

ALTER TABLE inventario DROP CONSTRAINT uq_inventario_centro_categoria;
ALTER TABLE inventario DROP COLUMN categoria;

ALTER TABLE inventario ALTER COLUMN item_catalogo_id SET NOT NULL;

ALTER TABLE inventario ADD CONSTRAINT uq_inventario_centro_item UNIQUE (centro_id, item_catalogo_id);

CREATE INDEX idx_inventario_item_catalogo_id ON inventario (item_catalogo_id);
