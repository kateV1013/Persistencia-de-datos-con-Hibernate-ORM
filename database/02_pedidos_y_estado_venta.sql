-- Quilate - tipos de servicio y pedidos personalizados
-- Ejecutar despues de 01_quilate_bd_inicial.sql.

CREATE TABLE IF NOT EXISTS tipo_servicio (
    id_tipo_servicio SERIAL PRIMARY KEY,
    nombre           VARCHAR(60) NOT NULL UNIQUE,
    descripcion      VARCHAR(255),
    precio_base      NUMERIC(10,2) NOT NULL CHECK (precio_base >= 0)
);

CREATE TABLE IF NOT EXISTS pedido_personalizado (
    id_pedido           SERIAL PRIMARY KEY,
    id_cliente          BIGINT NOT NULL REFERENCES cliente(id_cliente),
    id_tipo_servicio    INTEGER NOT NULL REFERENCES tipo_servicio(id_tipo_servicio),
    descripcion_trabajo VARCHAR(500) NOT NULL,
    fecha_programada    DATE NOT NULL,
    precio              NUMERIC(10,2) NOT NULL CHECK (precio >= 0),
    estado              VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'
                        CHECK (estado IN ('PENDIENTE', 'EN_PROCESO', 'TERMINADO', 'ENTREGADO', 'CANCELADO'))
);

CREATE INDEX IF NOT EXISTS idx_pedido_fecha_programada
    ON pedido_personalizado(fecha_programada);

INSERT INTO tipo_servicio (nombre, descripcion, precio_base) VALUES
('Grabado', 'Grabado de nombres, fechas o iniciales', 300.00),
('Reparacion', 'Soldadura, cambio de broche o reparacion de cadenas', 500.00),
('Ajuste de talla', 'Agrandar o reducir la talla de un anillo', 400.00),
('Limpieza y pulido', 'Limpieza ultrasonica y pulido de la pieza', 200.00)
ON CONFLICT (nombre) DO NOTHING;
