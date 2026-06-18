ALTER TABLE catalogo_items DROP COLUMN categoria;

ALTER TABLE movimientos_inventario ALTER COLUMN item_catalogo_id SET NOT NULL;
