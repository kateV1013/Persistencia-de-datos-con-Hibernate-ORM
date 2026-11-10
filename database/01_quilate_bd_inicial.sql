-- Quilate - esquema base unificado para PostgreSQL
-- Ejecutar conectado a una base vacia llamada quilate.

CREATE TABLE IF NOT EXISTS categorias (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS piezas (
    id            BIGSERIAL PRIMARY KEY,
    codigo_barras VARCHAR(50) NOT NULL UNIQUE,
    nombre        VARCHAR(200) NOT NULL,
    material      VARCHAR(50),
    quilates      SMALLINT,
    peso_gramos   NUMERIC(8,2),
    costo         DOUBLE PRECISION NOT NULL CHECK (costo >= 0),
    precio_venta  DOUBLE PRECISION NOT NULL CHECK (precio_venta >= costo),
    existencias   INTEGER NOT NULL DEFAULT 0 CHECK (existencias >= 0),
    categoria_id  BIGINT REFERENCES categorias(id)
);

CREATE TABLE IF NOT EXISTS cliente (
    id_cliente     BIGSERIAL PRIMARY KEY,
    nombre         VARCHAR(60) NOT NULL,
    apellido       VARCHAR(60),
    telefono       VARCHAR(20),
    correo         VARCHAR(100) UNIQUE,
    fecha_registro DATE NOT NULL DEFAULT CURRENT_DATE
);

CREATE TABLE IF NOT EXISTS apartado (
    id         BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES cliente(id_cliente),
    pieza_id   BIGINT NOT NULL REFERENCES piezas(id),
    cantidad   INTEGER NOT NULL CHECK (cantidad > 0),
    fecha      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado     VARCHAR(20) NOT NULL DEFAULT 'ACTIVO'
               CHECK (estado IN ('ACTIVO', 'CANCELADO', 'COMPLETADO'))
);

CREATE TABLE IF NOT EXISTS venta (
    id_venta                BIGSERIAL PRIMARY KEY,
    id_cliente              BIGINT NOT NULL REFERENCES cliente(id_cliente),
    fecha_hora              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metodo_pago             VARCHAR(20) NOT NULL DEFAULT 'EFECTIVO'
                            CHECK (metodo_pago IN ('EFECTIVO', 'TARJETA', 'TRANSFERENCIA')),
    total                   NUMERIC(14,2) NOT NULL DEFAULT 0 CHECK (total >= 0),
    estado                  VARCHAR(20) NOT NULL DEFAULT 'PREPARACION'
                            CHECK (estado IN ('PREPARACION', 'PENDIENTE', 'CONFIRMADA', 'ANULADA')),
    inventario_comprometido BOOLEAN NOT NULL DEFAULT FALSE,
    apartado_origen_id      BIGINT UNIQUE REFERENCES apartado(id),
    version                 BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS detalle_venta (
    id_detalle           BIGSERIAL PRIMARY KEY,
    id_venta             BIGINT NOT NULL REFERENCES venta(id_venta) ON DELETE CASCADE,
    id_pieza             BIGINT NOT NULL REFERENCES piezas(id),
    cantidad             INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario      NUMERIC(12,2) NOT NULL CHECK (precio_unitario > 0),
    costo_unitario_venta NUMERIC(12,2) NOT NULL CHECK (costo_unitario_venta >= 0),
    descuento_aplicado   NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (descuento_aplicado >= 0),
    CONSTRAINT descuento_rentable CHECK (
        precio_unitario - descuento_aplicado >= costo_unitario_venta
    )
);

CREATE INDEX IF NOT EXISTS idx_venta_cliente_fecha ON venta(id_cliente, fecha_hora DESC);
CREATE INDEX IF NOT EXISTS idx_detalle_venta_pieza ON detalle_venta(id_pieza);

INSERT INTO categorias (nombre, descripcion) VALUES
('ANILLO', 'Anillos y argollas'),
('COLLAR', 'Collares y cadenas'),
('ARETES', 'Aretes y pendientes'),
('PULSERA', 'Pulseras'),
('OTRO', 'Otras piezas')
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO cliente (nombre, apellido, telefono, correo) VALUES
('Maria', 'Lopez', '88881111', 'maria.lopez@correo.com'),
('Jose', 'Martinez', '87772222', 'jose.martinez@correo.com')
ON CONFLICT (correo) DO NOTHING;

INSERT INTO piezas (codigo_barras, nombre, material, quilates, peso_gramos,
                    costo, precio_venta, existencias, categoria_id)
SELECT datos.codigo, datos.nombre, datos.material, datos.quilates, datos.peso,
       datos.costo, datos.precio, datos.stock, c.id
FROM (VALUES
    ('7501000000011', 'Anillo solitario', 'Oro', 14::SMALLINT, 3.20::NUMERIC, 4500.00, 7000.00, 5, 'ANILLO'),
    ('7501000000028', 'Cadena cubana', 'Plata', NULL::SMALLINT, 12.50::NUMERIC, 1200.00, 2000.00, 10, 'COLLAR'),
    ('7501000000035', 'Argollas pequenas', 'Oro', 10::SMALLINT, 1.80::NUMERIC, 1800.00, 2600.00, 8, 'ARETES')
) AS datos(codigo, nombre, material, quilates, peso, costo, precio, stock, categoria)
JOIN categorias c ON c.nombre = datos.categoria
ON CONFLICT (codigo_barras) DO NOTHING;
