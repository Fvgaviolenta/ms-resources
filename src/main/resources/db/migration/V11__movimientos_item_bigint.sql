ALTER TABLE movimientos_inventario ADD COLUMN item_catalogo_id UUID REFERENCES catalogo_items(id);

UPDATE movimientos_inventario m
SET item_catalogo_id = i.item_catalogo_id
FROM inventario i
WHERE m.inventario_id = i.id;

ALTER TABLE movimientos_inventario ALTER COLUMN cantidad TYPE BIGINT;
ALTER TABLE movimientos_inventario ALTER COLUMN stock_anterior TYPE BIGINT;
ALTER TABLE movimientos_inventario ALTER COLUMN stock_posterior TYPE BIGINT;

CREATE INDEX idx_movimientos_inventario_item_catalogo_id ON movimientos_inventario (item_catalogo_id);
