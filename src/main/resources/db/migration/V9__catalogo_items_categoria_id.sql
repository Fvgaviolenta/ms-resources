ALTER TABLE catalogo_items ADD COLUMN categoria_id UUID REFERENCES categorias(id);

UPDATE catalogo_items ci
SET categoria_id = cat.id
FROM categorias cat
WHERE ci.categoria = cat.codigo;

ALTER TABLE catalogo_items ALTER COLUMN categoria_id SET NOT NULL;

CREATE INDEX idx_catalogo_items_categoria_id ON catalogo_items (categoria_id);
