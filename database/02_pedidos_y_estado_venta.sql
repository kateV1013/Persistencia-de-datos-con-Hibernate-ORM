-- =====================================================
-- Quilate - Migracion 02
-- Integrante 4: pedidos personalizados y reportes
-- Ejecutar DESPUES de 01_quilate_bd_inicial.sql, conectado a la base quilate.
-- Se puede ejecutar varias veces sin duplicar nada.
-- =====================================================

-- -----------------------------------------------------
-- VENTA: estado de la venta
-- El reporte de piezas mas vendidas solo cuenta ventas CONFIRMADAS.
-- Las ventas existentes quedan como CONFIRMADA.
-- -----------------------------------------------------
ALTER TABLE venta
    ADD COLUMN IF NOT EXISTS estado VARCHAR(20) NOT NULL DEFAULT 'CONFIRMADA'
        CHECK (estado IN ('PENDIENTE', 'CONFIRMADA', 'ANULADA'));

-- -----------------------------------------------------
-- TIPO_SERVICIO: grabado, reparacion, ajuste de talla...
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS tipo_servicio (
    id_tipo_servicio  SERIAL PRIMARY KEY,
    nombre            VARCHAR(60)   NOT NULL UNIQUE,
    descripcion       VARCHAR(255),
    precio_base       NUMERIC(10,2) NOT NULL CHECK (precio_base >= 0)
);

-- -----------------------------------------------------
-- PEDIDO_PERSONALIZADO
-- Un cliente tiene muchos pedidos; un tipo de servicio se usa en muchos pedidos.
-- Cupo: maximo 20 pedidos activos (estado distinto de CANCELADO) por semana
-- de lunes a domingo. Se valida en la aplicacion (PedidoService).
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS pedido_personalizado (
    id_pedido            SERIAL PRIMARY KEY,
    id_cliente           INT NOT NULL REFERENCES cliente(id_cliente),
    id_tipo_servicio     INT NOT NULL REFERENCES tipo_servicio(id_tipo_servicio),
    descripcion_trabajo  VARCHAR(500)  NOT NULL,
    fecha_programada     DATE          NOT NULL,
    precio               NUMERIC(10,2) NOT NULL CHECK (precio >= 0),
    estado               VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE'
                         CHECK (estado IN ('PENDIENTE', 'EN_PROCESO', 'TERMINADO', 'ENTREGADO', 'CANCELADO'))
);

CREATE INDEX IF NOT EXISTS idx_pedido_fecha_programada
    ON pedido_personalizado (fecha_programada);

-- -----------------------------------------------------
-- Datos de prueba
-- -----------------------------------------------------
INSERT INTO tipo_servicio (nombre, descripcion, precio_base) VALUES
('Grabado',           'Grabado de nombres, fechas o iniciales',               300.00),
('Reparación',        'Soldadura, cambio de broche o reparación de cadenas', 500.00),
('Ajuste de talla',   'Agrandar o reducir la talla de un anillo',            400.00),
('Limpieza y pulido', 'Limpieza ultrasónica y pulido de la pieza',           200.00)
ON CONFLICT (nombre) DO NOTHING;
