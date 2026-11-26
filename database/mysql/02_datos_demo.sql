-- Ejecutar una vez, después de 01_esquema.sql, en la base de demostración.
START TRANSACTION;
INSERT INTO categorias (nombre, descripcion) VALUES ('Anillos demo', 'Anillos para la defensa');
SET @categoria = LAST_INSERT_ID();
INSERT INTO piezas (codigo_barras, nombre, costo, precio_venta, existencias, categoria_id)
VALUES ('DEMO-001', 'Anillo Aurora', 900.00, 1800.00, 9, @categoria);
SET @pieza = LAST_INSERT_ID();
INSERT INTO cliente (nombre, apellido, telefono, correo)
VALUES ('Ana', 'Demo', '85815705', 'ana.demo@example.test');
SET @cliente = LAST_INSERT_ID();
INSERT INTO tipo_servicio (nombre, precio_base) VALUES ('Grabado demo', 350.00);
SET @servicio = LAST_INSERT_ID();
INSERT INTO pedido_personalizado (id_cliente, id_tipo_servicio, descripcion_trabajo, fecha_programada, precio)
VALUES (@cliente, @servicio, 'Grabar iniciales A. D.', DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 350.00);
INSERT INTO apartado (cliente_id, pieza_id, cantidad, estado) VALUES (@cliente, @pieza, 1, 'ACTIVO');
INSERT INTO venta (id_cliente, total, estado, inventario_comprometido)
VALUES (@cliente, 1800.00, 'CONFIRMADA', TRUE);
INSERT INTO detalle_venta (id_venta, id_pieza, cantidad, precio_unitario, costo_unitario_venta)
VALUES (LAST_INSERT_ID(), @pieza, 1, 1800.00, 900.00);
COMMIT;
