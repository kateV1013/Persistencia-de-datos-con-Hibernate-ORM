-- =====================================================
-- Quilate: Sistema de Ventas y Análisis de Clientes
-- Base de datos inicial (PostgreSQL)
-- Una tabla por integrante
-- =====================================================

-- Crear la base primero (pgAdmin o psql) y ejecutar este script conectado a ella:
--   CREATE DATABASE quilate;

-- -----------------------------------------------------
-- Integrante 1: CLIENTE
-- Perfil de cada cliente (el historial sale de sus ventas)
-- -----------------------------------------------------
CREATE TABLE cliente (
    id_cliente      SERIAL PRIMARY KEY,
    nombre          VARCHAR(60)  NOT NULL,
    apellido        VARCHAR(60)  NOT NULL,
    telefono        VARCHAR(15),
    correo          VARCHAR(100) UNIQUE,
    fecha_registro  DATE NOT NULL DEFAULT CURRENT_DATE
);

-- -----------------------------------------------------
-- Integrante 2: PIEZA
-- Cada joya, identificada por su código de barras
-- -----------------------------------------------------
CREATE TABLE pieza (
    id_pieza             SERIAL PRIMARY KEY,
    codigo_barras        VARCHAR(30)   NOT NULL UNIQUE,
    nombre               VARCHAR(100)  NOT NULL,
    material             VARCHAR(50)   NOT NULL,      -- oro, plata, acero...
    quilates             SMALLINT,                    -- NULL si no aplica
    peso_gramos          NUMERIC(6,2),
    costo                NUMERIC(10,2) NOT NULL CHECK (costo > 0),
    precio_venta         NUMERIC(10,2) NOT NULL,
    categoria            VARCHAR(20)   NOT NULL
                         CHECK (categoria IN ('ANILLO', 'COLLAR', 'ARETES', 'PULSERA', 'OTRO')),
    cantidad_disponible  INT NOT NULL DEFAULT 0 CHECK (cantidad_disponible >= 0),
    CONSTRAINT precio_mayor_costo CHECK (precio_venta > costo)
);

-- -----------------------------------------------------
-- Integrante 3: VENTA
-- Fecha y hora permiten analizar días y horarios de compra
-- -----------------------------------------------------
CREATE TABLE venta (
    id_venta     SERIAL PRIMARY KEY,
    id_cliente   INT NOT NULL REFERENCES cliente(id_cliente),
    fecha_hora   TIMESTAMP NOT NULL DEFAULT NOW(),
    metodo_pago  VARCHAR(20) NOT NULL
                 CHECK (metodo_pago IN ('EFECTIVO', 'TARJETA', 'TRANSFERENCIA'))
);

-- -----------------------------------------------------
-- Integrante 4: DETALLE_VENTA
-- Piezas de cada venta y el descuento aplicado
-- -----------------------------------------------------
CREATE TABLE detalle_venta (
    id_detalle       SERIAL PRIMARY KEY,
    id_venta         INT NOT NULL REFERENCES venta(id_venta) ON DELETE CASCADE,
    id_pieza         INT NOT NULL REFERENCES pieza(id_pieza),
    cantidad         INT NOT NULL CHECK (cantidad > 0),
    precio_unitario  NUMERIC(10,2) NOT NULL,
    descuento_pct    NUMERIC(5,2) NOT NULL DEFAULT 0
                     CHECK (descuento_pct BETWEEN 0 AND 100),
    subtotal         NUMERIC(12,2) GENERATED ALWAYS AS
                     (cantidad * precio_unitario * (1 - descuento_pct / 100)) STORED
);

-- =====================================================
-- LÍMITE: descuento máximo rentable
-- Bloquea un descuento que deje la pieza por debajo de su costo
-- =====================================================
CREATE OR REPLACE FUNCTION validar_descuento()
RETURNS TRIGGER AS $$
DECLARE
    v_costo      NUMERIC(10,2);
    v_max_desc   NUMERIC(5,2);
BEGIN
    SELECT costo INTO v_costo FROM pieza WHERE id_pieza = NEW.id_pieza;

    IF NEW.precio_unitario * (1 - NEW.descuento_pct / 100) < v_costo THEN
        v_max_desc := ROUND((1 - v_costo / NEW.precio_unitario) * 100, 2);
        RAISE EXCEPTION 'Descuento no rentable. El máximo permitido es %',
                        v_max_desc || '%';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_validar_descuento
BEFORE INSERT OR UPDATE ON detalle_venta
FOR EACH ROW EXECUTE FUNCTION validar_descuento();

-- =====================================================
-- Datos de prueba
-- =====================================================
INSERT INTO cliente (nombre, apellido, telefono, correo) VALUES
('María', 'López', '88881111', 'maria.lopez@correo.com'),
('José', 'Martínez', '87772222', 'jose.martinez@correo.com');

INSERT INTO pieza (codigo_barras, nombre, material, quilates, peso_gramos,
                   costo, precio_venta, categoria, cantidad_disponible) VALUES
('7501000000011', 'Anillo solitario', 'Oro', 14, 3.20, 4500.00, 7000.00, 'ANILLO', 5),
('7501000000028', 'Cadena cubana',    'Plata', NULL, 12.50, 1200.00, 2000.00, 'COLLAR', 10),
('7501000000035', 'Argollas pequeñas','Oro', 10, 1.80, 1800.00, 2600.00, 'ARETES', 8);

INSERT INTO venta (id_cliente, metodo_pago) VALUES (1, 'TARJETA');

INSERT INTO detalle_venta (id_venta, id_pieza, cantidad, precio_unitario, descuento_pct)
VALUES (1, 1, 1, 7000.00, 10);   -- 10% sí es rentable (máximo ~35.71%)
