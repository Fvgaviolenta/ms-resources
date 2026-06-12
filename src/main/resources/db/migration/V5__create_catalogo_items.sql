CREATE TABLE catalogo_items (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre          VARCHAR(200) NOT NULL,
    categoria       VARCHAR(50) NOT NULL,
    descripcion     VARCHAR(500),
    unidad_medida   VARCHAR(30) NOT NULL DEFAULT 'UNIDAD',
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_catalogo_items_categoria ON catalogo_items (categoria);
CREATE INDEX idx_catalogo_items_activo ON catalogo_items (activo);

-- Semilla minima por categoria (referencia para donaciones y dashboard)
INSERT INTO catalogo_items (nombre, categoria, descripcion, unidad_medida) VALUES
    ('Arroz', 'ALIMENTOS', 'Arroz grano largo', 'KG'),
    ('Agua embotellada', 'ALIMENTOS', 'Bidones o botellas', 'UNIDAD'),
    ('Jabon', 'ARTICULOS_HIGIENE', 'Jabon de tocador', 'UNIDAD'),
    ('Panales', 'ARTICULOS_HIGIENE', 'Panales desechables', 'UNIDAD'),
    ('Palas', 'HERRAMIENTAS', 'Palas de excavacion', 'UNIDAD'),
    ('Martillos', 'HERRAMIENTAS', 'Martillos de carpintero', 'UNIDAD'),
    ('Frazadas', 'MATERIALES', 'Frazadas termicas', 'UNIDAD'),
    ('Carpas', 'MATERIALES', 'Carpas familiares', 'UNIDAD'),
    ('Chaquetas', 'ROPA', 'Chaquetas abrigadoras', 'UNIDAD'),
    ('Zapatos', 'ROPA', 'Calzado variado', 'PAR'),
    ('Utensilios varios', 'ARTICULOS_VARIOS', 'Articulos de uso general', 'UNIDAD');
