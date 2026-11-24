-- MySQL 8.4. Ejecutar en una base vacia seleccionada en la sesion.

CREATE TABLE IF NOT EXISTS categorias (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(500)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS piezas (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo_barras VARCHAR(50) NOT NULL UNIQUE,
    nombre        VARCHAR(200) NOT NULL,
    material      VARCHAR(50),
    quilates      SMALLINT,
    peso_gramos   NUMERIC(8,2),
    costo         DOUBLE PRECISION NOT NULL CHECK (costo >= 0),
    precio_venta  DOUBLE PRECISION NOT NULL CHECK (precio_venta >= costo),
    existencias   INTEGER NOT NULL DEFAULT 0 CHECK (existencias >= 0),
    categoria_id   BIGINT,
    FOREIGN KEY (categoria_id) REFERENCES categorias(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cliente (
    id_cliente     BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(60) NOT NULL,
    apellido       VARCHAR(60),
    telefono       VARCHAR(20),
    correo         VARCHAR(100) UNIQUE,
    fecha_registro DATE NOT NULL DEFAULT (CURRENT_DATE)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS apartado (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id  BIGINT NOT NULL,
    pieza_id    BIGINT NOT NULL,
    cantidad   INTEGER NOT NULL CHECK (cantidad > 0),
    fecha      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    estado     VARCHAR(20) NOT NULL DEFAULT 'ACTIVO'
               CHECK (estado IN ('ACTIVO', 'CANCELADO', 'COMPLETADO')),
    FOREIGN KEY (cliente_id) REFERENCES cliente(id_cliente),
    FOREIGN KEY (pieza_id) REFERENCES piezas(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS venta (
    id_venta                BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_cliente               BIGINT NOT NULL,
    fecha_hora              DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    metodo_pago             VARCHAR(20) NOT NULL DEFAULT 'EFECTIVO'
                            CHECK (metodo_pago IN ('EFECTIVO', 'TARJETA', 'TRANSFERENCIA')),
    total                   NUMERIC(14,2) NOT NULL DEFAULT 0 CHECK (total >= 0),
    estado                  VARCHAR(20) NOT NULL DEFAULT 'PREPARACION'
                            CHECK (estado IN ('PREPARACION', 'PENDIENTE', 'CONFIRMADA', 'ANULADA')),
    inventario_comprometido BOOLEAN NOT NULL DEFAULT FALSE,
    apartado_origen_id       BIGINT UNIQUE,
    version                 BIGINT NOT NULL DEFAULT 0,
    FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente),
    FOREIGN KEY (apartado_origen_id) REFERENCES apartado(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS detalle_venta (
    id_detalle           BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_venta              BIGINT NOT NULL,
    id_pieza              BIGINT NOT NULL,
    cantidad             INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario      NUMERIC(12,2) NOT NULL CHECK (precio_unitario > 0),
    costo_unitario_venta NUMERIC(12,2) NOT NULL CHECK (costo_unitario_venta >= 0),
    descuento_aplicado   NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (descuento_aplicado >= 0),
    CONSTRAINT descuento_rentable CHECK (
        precio_unitario - descuento_aplicado >= costo_unitario_venta
    ),
    FOREIGN KEY (id_venta) REFERENCES venta(id_venta) ON DELETE CASCADE,
    FOREIGN KEY (id_pieza) REFERENCES piezas(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tipo_servicio (
    id_tipo_servicio INTEGER AUTO_INCREMENT PRIMARY KEY,
    nombre           VARCHAR(60) NOT NULL UNIQUE,
    descripcion      VARCHAR(255),
    precio_base      NUMERIC(10,2) NOT NULL CHECK (precio_base >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS pedido_personalizado (
    id_pedido           INTEGER AUTO_INCREMENT PRIMARY KEY,
    id_cliente           BIGINT NOT NULL,
    id_tipo_servicio     INTEGER NOT NULL,
    descripcion_trabajo VARCHAR(500) NOT NULL,
    fecha_programada    DATE NOT NULL,
    precio              NUMERIC(10,2) NOT NULL CHECK (precio >= 0),
    diseno_texto        VARCHAR(40),
    diseno_forma        VARCHAR(16),
    diseno_acabado      VARCHAR(16),
    diseno_fuente       VARCHAR(16),
    diseno_medida_mm    NUMERIC(5,2),
    diseno_ancho_mm     NUMERIC(5,2),
    diseno_letra_mm     NUMERIC(4,2),
    estado              VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'
                        CHECK (estado IN ('PENDIENTE', 'EN_PROCESO', 'TERMINADO', 'ENTREGADO', 'CANCELADO')),
    FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente),
    FOREIGN KEY (id_tipo_servicio) REFERENCES tipo_servicio(id_tipo_servicio)
) ENGINE=InnoDB;
