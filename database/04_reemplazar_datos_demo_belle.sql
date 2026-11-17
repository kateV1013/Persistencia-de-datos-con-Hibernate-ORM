-- JOYERIA BELLE: DATOS FICTICIOS PARA EL PROYECTO, importes en C$.
-- Ejecutar despues de 01, 02 y 03, con la aplicacion cerrada.
-- ATENCION: reemplaza TODOS los datos comerciales. Respaldar antes de ejecutar.
-- No representa el catalogo, clientes ni servicios reales de Belle.
BEGIN;
-- CURRENT_DATE y CURRENT_TIMESTAMP en hora de Managua, igual que la aplicacion.
SET LOCAL TIME ZONE 'America/Managua';
TRUNCATE TABLE detalle_venta, venta, apartado, pedido_personalizado,
    piezas, cliente, tipo_servicio, categorias RESTART IDENTITY;
INSERT INTO categorias (nombre, descripcion) VALUES
('ANILLO','Anillos y argollas para mujer, hombre y unisex'),
('COLLAR','Collares y cadenas para mujer, hombre y unisex'),
('ARETES','Aretes y pendientes'),
('PULSERA','Pulseras y esclavas para mujer, hombre y unisex'),
('OTRO','Dijes y placas');

INSERT INTO cliente (nombre,apellido,telefono,correo) VALUES
('Valeria', 'Mendoza', NULL, 'belle.demo.001@example.com'),
('Diego', 'Solis', NULL, 'belle.demo.002@example.com'),
('Camila', 'Rivas', NULL, 'belle.demo.003@example.com'),
('Andres', 'Salazar', NULL, 'belle.demo.004@example.com'),
('Daniela', 'Vega', NULL, 'belle.demo.005@example.com'),
('Mateo', 'Duarte', NULL, 'belle.demo.006@example.com'),
('Lucia', 'Pineda', NULL, 'belle.demo.007@example.com'),
('Gabriel', 'Larios', NULL, 'belle.demo.008@example.com'),
('Sofia', 'Castillo', NULL, 'belle.demo.009@example.com'),
('Nicolas', 'Morales', NULL, 'belle.demo.010@example.com'),
('Mariana', 'Zamora', NULL, 'belle.demo.011@example.com'),
('Sebastian', 'Reyes', NULL, 'belle.demo.012@example.com'),
('Isabella', 'Navarro', NULL, 'belle.demo.013@example.com'),
('Alejandro', 'Torres', NULL, 'belle.demo.014@example.com'),
('Renata', 'Aguilar', NULL, 'belle.demo.015@example.com'),
('Santiago', 'Molina', NULL, 'belle.demo.016@example.com'),
('Paula', 'Estrada', NULL, 'belle.demo.017@example.com'),
('Emilio', 'Flores', NULL, 'belle.demo.018@example.com'),
('Adriana', 'Bermudez', NULL, 'belle.demo.019@example.com'),
('David', 'Vargas', NULL, 'belle.demo.020@example.com'),
('Natalia', 'Mendoza', NULL, 'belle.demo.021@example.com'),
('Joaquin', 'Solis', NULL, 'belle.demo.022@example.com'),
('Elena', 'Rivas', NULL, 'belle.demo.023@example.com'),
('Samuel', 'Salazar', NULL, 'belle.demo.024@example.com'),
('Carolina', 'Vega', NULL, 'belle.demo.025@example.com'),
('Leonardo', 'Duarte', NULL, 'belle.demo.026@example.com'),
('Victoria', 'Pineda', NULL, 'belle.demo.027@example.com'),
('Esteban', 'Larios', NULL, 'belle.demo.028@example.com'),
('Amanda', 'Castillo', NULL, 'belle.demo.029@example.com'),
('Rafael', 'Morales', NULL, 'belle.demo.030@example.com'),
('Valeria', 'Flores', NULL, 'belle.demo.031@example.com'),
('Diego', 'Bermudez', NULL, 'belle.demo.032@example.com'),
('Camila', 'Vargas', NULL, 'belle.demo.033@example.com'),
('Andres', 'Mendoza', NULL, 'belle.demo.034@example.com'),
('Daniela', 'Solis', NULL, 'belle.demo.035@example.com'),
('Mateo', 'Rivas', NULL, 'belle.demo.036@example.com'),
('Lucia', 'Salazar', NULL, 'belle.demo.037@example.com'),
('Gabriel', 'Vega', NULL, 'belle.demo.038@example.com'),
('Sofia', 'Duarte', NULL, 'belle.demo.039@example.com'),
('Nicolas', 'Pineda', NULL, 'belle.demo.040@example.com'),
('Mariana', 'Larios', NULL, 'belle.demo.041@example.com'),
('Sebastian', 'Castillo', NULL, 'belle.demo.042@example.com'),
('Isabella', 'Morales', NULL, 'belle.demo.043@example.com'),
('Alejandro', 'Zamora', NULL, 'belle.demo.044@example.com'),
('Renata', 'Reyes', NULL, 'belle.demo.045@example.com'),
('Santiago', 'Navarro', NULL, 'belle.demo.046@example.com'),
('Paula', 'Torres', NULL, 'belle.demo.047@example.com'),
('Emilio', 'Aguilar', NULL, 'belle.demo.048@example.com'),
('Adriana', 'Molina', NULL, 'belle.demo.049@example.com'),
('David', 'Estrada', NULL, 'belle.demo.050@example.com'),
('Natalia', 'Flores', NULL, 'belle.demo.051@example.com'),
('Joaquin', 'Bermudez', NULL, 'belle.demo.052@example.com'),
('Elena', 'Vargas', NULL, 'belle.demo.053@example.com'),
('Samuel', 'Mendoza', NULL, 'belle.demo.054@example.com'),
('Carolina', 'Solis', NULL, 'belle.demo.055@example.com'),
('Leonardo', 'Rivas', NULL, 'belle.demo.056@example.com'),
('Victoria', 'Salazar', NULL, 'belle.demo.057@example.com'),
('Esteban', 'Vega', NULL, 'belle.demo.058@example.com'),
('Amanda', 'Duarte', NULL, 'belle.demo.059@example.com'),
('Rafael', 'Pineda', NULL, 'belle.demo.060@example.com');

-- Todos los clientes anteceden a sus compras y pedidos. Sin telefonos reales.
UPDATE cliente SET fecha_registro = CURRENT_DATE - (150 + id_cliente::integer);

-- 16 modelos base. El stock se recalcula al final a partir de 40 unidades iniciales.
INSERT INTO piezas (codigo_barras, nombre, material, quilates, peso_gramos,
                    costo, precio_venta, existencias, categoria_id)
SELECT d.codigo, d.nombre, d.material, d.quilates, d.peso, d.costo, d.precio, d.stock, c.id
FROM (VALUES
('JB-001','Anillo Aurora - Mujer','Oro',14,2.80,5200,7800,6,'ANILLO'),
('JB-002','Anillo Sello - Hombre','Plata 925',NULL,8.20,1100,1800,8,'ANILLO'),
('JB-003','Argolla Horizonte - Unisex','Oro',18,4.50,9200,13800,4,'ANILLO'),
('JB-004','Anillo Luna - Mujer','Plata 925',NULL,3.10,650,1100,12,'ANILLO'),
('JB-005','Cadena Cubana - Hombre','Plata 925',NULL,18.50,2100,3400,7,'COLLAR'),
('JB-006','Collar Alba - Mujer','Oro',14,3.60,6200,9400,5,'COLLAR'),
('JB-007','Cadena Eslabon - Unisex','Oro',18,6.00,12500,18200,3,'COLLAR'),
('JB-008','Collar Brisa - Mujer','Plata 925',NULL,5.20,850,1450,10,'COLLAR'),
('JB-009','Aretes Destello - Mujer','Oro',14,1.90,3400,5200,8,'ARETES'),
('JB-010','Aretes Punto - Unisex','Plata 925',NULL,1.50,280,550,15,'ARETES'),
('JB-011','Argollas Luz - Mujer','Plata 925',NULL,3.40,600,1000,9,'ARETES'),
('JB-012','Esclava Fortaleza - Hombre','Plata 925',NULL,15.00,1600,2600,6,'PULSERA'),
('JB-013','Pulsera Estrella - Mujer','Oro',14,3.20,5700,8600,4,'PULSERA'),
('JB-014','Pulsera Lazo - Unisex','Plata 925',NULL,7.00,850,1400,10,'PULSERA'),
('JB-015','Dije Cruz - Hombre','Oro',18,2.50,4900,7400,5,'OTRO'),
('JB-016','Dije Inicial - Unisex','Plata 925',NULL,2.00,350,650,14,'OTRO')
) AS d(codigo,nombre,material,quilates,peso,costo,precio,stock,categoria)
JOIN categorias c ON c.nombre = d.categoria;


-- 48 variantes con medidas distintas; conservan el material del modelo base.
INSERT INTO piezas (codigo_barras,nombre,material,quilates,peso_gramos,costo,precio_venta,existencias,categoria_id)
SELECT 'JB-' || lpad((16 + (v.n-1)*16 + p.id)::text,3,'0'),
       p.nombre || ' / ' ||
       CASE c.nombre
         WHEN 'ANILLO' THEN (ARRAY['talla 6','talla 8','talla 10'])[v.n]
         WHEN 'COLLAR' THEN (ARRAY['45 cm','50 cm','60 cm'])[v.n]
         WHEN 'PULSERA' THEN (ARRAY['17 cm','19 cm','21 cm'])[v.n]
         WHEN 'ARETES' THEN (ARRAY['compactos','medianos','grandes'])[v.n]
         ELSE (ARRAY['18 mm','24 mm','30 mm'])[v.n] END,
       p.material,p.quilates,round(p.peso_gramos*(1+v.n*0.10),2),
       round((p.costo*(1+v.n*0.12))::numeric,2),
       round((p.precio_venta*(1+v.n*0.12))::numeric,2),40,p.categoria_id
FROM piezas p JOIN categorias c ON c.id=p.categoria_id
CROSS JOIN generate_series(1,3) v(n)
WHERE p.id<=16 ORDER BY v.n,p.id;
UPDATE piezas SET existencias=40;

INSERT INTO tipo_servicio (nombre,descripcion,precio_base) VALUES
('Grabado interior de anillo', 'DEMO: iniciales o dedicatoria en anillo aportado por el cliente', 350),
('Grabado de placa para esclava', 'DEMO: grabado frontal en placa de pulsera aportada por el cliente', 400),
('Grabado de placa para dije', 'DEMO: nombre o fecha sobre placa de dije aportada por el cliente', 300),
('Grabado de aniversario', 'DEMO: dedicatoria interior en argolla aportada por el cliente', 450),
('Ajuste de talla', 'DEMO: ajuste de anillo sin grabado; revisar viabilidad en taller', 450),
('Reparacion de broche', 'DEMO: reparar cierre de cadena aportada por el cliente', 500),
('Limpieza y pulido', 'DEMO: mantenimiento de joya aportada por el cliente', 250),
('Revision de engaste', 'DEMO: revisar y asegurar piedra existente; no incluye piedra nueva', 600);

-- 120 ventas: 100 confirmadas, 10 anuladas (stock reintegrado) y 10 borradores.
-- inventario_comprometido conserva TRUE al anular, como hace VentaService.
INSERT INTO venta (id_cliente,fecha_hora,metodo_pago,total,estado,inventario_comprometido)
SELECT 1+(n-1)%60,
       CURRENT_DATE - (CASE WHEN n>110 THEN 0 ELSE 1+(n*7)%90 END)
         + make_interval(hours => 10+n%7, mins => (n*11)%60),
       (ARRAY['EFECTIVO','TARJETA','TRANSFERENCIA'])[1+(n-1)%3],0,
       CASE WHEN n<=100 THEN 'CONFIRMADA' WHEN n<=110 THEN 'ANULADA' ELSE 'PREPARACION' END,
       n<=110
FROM generate_series(1,120) g(n) ORDER BY n;
-- Borradores de hoy con hora actual; nunca movimientos futuros.
UPDATE venta SET fecha_hora=CURRENT_TIMESTAMP WHERE estado='PREPARACION';
INSERT INTO detalle_venta (id_venta,id_pieza,cantidad,precio_unitario,costo_unitario_venta,descuento_aplicado)
SELECT v.id_venta,p.id,CASE WHEN l.n=2 AND v.id_venta%5=0 THEN 2 ELSE 1 END,
       p.precio_venta,p.costo,
       CASE WHEN v.id_venta%4=0 THEN round((p.precio_venta*0.05)::numeric,2) ELSE 0 END
FROM venta v CROSS JOIN generate_series(1,2) l(n)
JOIN piezas p ON p.id=1+((v.id_venta-1)*3+(l.n-1)*17)%64
ORDER BY v.id_venta,l.n;
UPDATE venta v SET total=(SELECT SUM(d.cantidad*(d.precio_unitario-d.descuento_aplicado))
                          FROM detalle_venta d WHERE d.id_venta=v.id_venta);

-- 12 reservas activas, 6 canceladas y 6 completadas, enlazadas a ventas.
INSERT INTO apartado (cliente_id,pieza_id,cantidad,fecha,estado)
SELECT 1+(n*7)%60,1+(n*5)%64,1,CURRENT_DATE-(1+n%12),
       CASE WHEN n<=12 THEN 'ACTIVO' ELSE 'CANCELADO' END
FROM generate_series(1,18) g(n) ORDER BY n;
INSERT INTO apartado (cliente_id,pieza_id,cantidad,fecha,estado)
SELECT v.id_cliente,d.id_pieza,d.cantidad,v.fecha_hora-INTERVAL '5 days','COMPLETADO'
FROM venta v JOIN detalle_venta d ON d.id_venta=v.id_venta
WHERE v.id_venta BETWEEN 1 AND 6 AND d.id_detalle%2=1 ORDER BY v.id_venta;
-- Cada venta desde apartado contiene unicamente la pieza originalmente reservada.
DELETE FROM detalle_venta WHERE id_venta BETWEEN 1 AND 6 AND id_detalle%2=0;
UPDATE venta v SET apartado_origen_id=v.id_venta+18,
 total=(SELECT SUM(d.cantidad*(d.precio_unitario-d.descuento_aplicado))
        FROM detalle_venta d WHERE d.id_venta=v.id_venta)
WHERE v.id_venta BETWEEN 1 AND 6;
-- Saldo disponible: 40 iniciales menos ventas confirmadas y apartados activos.
-- Los completados ya se cuentan en sus ventas; los anulados/cancelados no restan.
UPDATE piezas p SET existencias=40
 - COALESCE((SELECT SUM(d.cantidad) FROM detalle_venta d JOIN venta v USING(id_venta)
             WHERE d.id_pieza=p.id AND v.estado='CONFIRMADA'),0)
 - COALESCE((SELECT SUM(a.cantidad) FROM apartado a
             WHERE a.pieza_id=p.id AND a.estado='ACTIVO'),0);

-- 60 pedidos distribuidos en 10 semanas (6 por semana, menos que el cupo de 20).
-- El precio cubre solo el trabajo: todas las piezas son aportadas por el cliente.
-- 48 grabados con los 7 campos del editor; 12 mantenimientos sin diseno.

INSERT INTO pedido_personalizado (id_cliente,id_tipo_servicio,descripcion_trabajo,precio,estado,diseno_texto,diseno_forma,diseno_acabado,diseno_fuente,diseno_medida_mm,diseno_ancho_mm,diseno_letra_mm,fecha_programada) VALUES

(1, 1, 'DEMO: anillo aportado por Valeria Mendoza. Grabar "V. M.". Acabado visual Oro; fuente Clásica; diametro interior 17.0 mm y banda 4 mm; letra 1.5 mm. Revisar espacio en taller.', 350, 'ENTREGADO', 'V. M.', 'Anillo', 'Oro', 'Clásica', 17.0, 4, 1.5, (date_trunc('week',CURRENT_DATE)::date + (-5 * 7) + 0)),
(2, 2, 'DEMO: placa de esclava aportada por Diego Solis. Grabar "Diego". Acabado visual Oro; fuente Clásica; 35 x 12 mm; letra 2.0 mm. Forma: Placa. Revisar espacio en taller.', 400, 'ENTREGADO', 'Diego', 'Placa', 'Oro', 'Clásica', 35, 12, 2.0, (date_trunc('week',CURRENT_DATE)::date + (-5 * 7) + 1)),
(3, 3, 'DEMO: placa de dije aportada por Camila Rivas. Grabar "Mi familia". Acabado visual Oro; fuente Clásica; 38 x 14 mm; letra 2.0 mm. Forma: Placa ovalada. Revisar espacio en taller.', 300, 'ENTREGADO', 'Mi familia', 'Placa ovalada', 'Oro', 'Clásica', 38, 14, 2.0, (date_trunc('week',CURRENT_DATE)::date + (-5 * 7) + 2)),
(4, 4, 'DEMO: argolla aportada por Andres Salazar. Grabar "Siempre juntos". Acabado visual Oro; fuente Clásica; diametro interior 18.8 mm y banda 4 mm; letra 1.5 mm. Revisar espacio en taller.', 500, 'ENTREGADO', 'Siempre juntos', 'Anillo', 'Oro', 'Clásica', 18.8, 4, 1.5, (date_trunc('week',CURRENT_DATE)::date + (-5 * 7) + 3)),
(5, 1, 'DEMO: anillo aportado por Daniela Vega. Grabar "D. V.". Acabado visual Plata; fuente Clásica; diametro interior 19.4 mm y banda 5 mm; letra 1.5 mm. Revisar espacio en taller.', 350, 'ENTREGADO', 'D. V.', 'Anillo', 'Plata', 'Clásica', 19.4, 5, 1.5, (date_trunc('week',CURRENT_DATE)::date + (-5 * 7) + 4)),
(6, 2, 'DEMO: placa de esclava aportada por Mateo Duarte. Grabar "Mateo". Acabado visual Plata; fuente Clásica; 25 x 25 mm; letra 2.0 mm. Forma: Placa circular. Revisar espacio en taller.', 400, 'ENTREGADO', 'Mateo', 'Placa circular', 'Plata', 'Clásica', 25, 25, 2.0, (date_trunc('week',CURRENT_DATE)::date + (-5 * 7) + 5)),
(7, 3, 'DEMO: placa de dije aportada por Lucia Pineda. Grabar "Mi familia". Acabado visual Plata; fuente Clásica; 38 x 14 mm; letra 2.0 mm. Forma: Placa corazón. Revisar espacio en taller.', 300, 'ENTREGADO', 'Mi familia', 'Placa corazón', 'Plata', 'Clásica', 38, 14, 2.0, (date_trunc('week',CURRENT_DATE)::date + (-4 * 7) + 0)),
(8, 4, 'DEMO: argolla aportada por Gabriel Larios. Grabar "Siempre juntos". Acabado visual Plata; fuente Clásica; diametro interior 18.2 mm y banda 5 mm; letra 1.5 mm. Revisar espacio en taller.', 500, 'ENTREGADO', 'Siempre juntos', 'Anillo', 'Plata', 'Clásica', 18.2, 5, 1.5, (date_trunc('week',CURRENT_DATE)::date + (-4 * 7) + 1)),
(9, 1, 'DEMO: anillo aportado por Sofia Castillo. Grabar "S. C.". Acabado visual Oro rosa; fuente Clásica; diametro interior 18.8 mm y banda 6 mm; letra 1.5 mm. Revisar espacio en taller.', 350, 'ENTREGADO', 'S. C.', 'Anillo', 'Oro rosa', 'Clásica', 18.8, 6, 1.5, (date_trunc('week',CURRENT_DATE)::date + (-4 * 7) + 2)),
(10, 2, 'DEMO: placa de esclava aportada por Nicolas Morales. Grabar "Nicolas". Acabado visual Oro rosa; fuente Clásica; 35 x 12 mm; letra 2.0 mm. Forma: Placa. Revisar espacio en taller.', 400, 'ENTREGADO', 'Nicolas', 'Placa', 'Oro rosa', 'Clásica', 35, 12, 2.0, (date_trunc('week',CURRENT_DATE)::date + (-4 * 7) + 3)),
(11, 3, 'DEMO: placa de dije aportada por Mariana Zamora. Grabar "Mi familia". Acabado visual Oro rosa; fuente Clásica; 38 x 14 mm; letra 2.0 mm. Forma: Placa ovalada. Revisar espacio en taller.', 300, 'ENTREGADO', 'Mi familia', 'Placa ovalada', 'Oro rosa', 'Clásica', 38, 14, 2.0, (date_trunc('week',CURRENT_DATE)::date + (-4 * 7) + 4)),
(12, 4, 'DEMO: argolla aportada por Sebastian Reyes. Grabar "Siempre juntos". Acabado visual Oro rosa; fuente Clásica; diametro interior 17.6 mm y banda 6 mm; letra 1.5 mm. Revisar espacio en taller.', 500, 'ENTREGADO', 'Siempre juntos', 'Anillo', 'Oro rosa', 'Clásica', 17.6, 6, 1.5, (date_trunc('week',CURRENT_DATE)::date + (-4 * 7) + 5)),
(13, 1, 'DEMO: anillo aportado por Isabella Navarro. Grabar "I. N.". Acabado visual Oro; fuente Moderna; diametro interior 18.2 mm y banda 4 mm; letra 1.5 mm. Revisar espacio en taller.', 350, 'ENTREGADO', 'I. N.', 'Anillo', 'Oro', 'Moderna', 18.2, 4, 1.5, (date_trunc('week',CURRENT_DATE)::date + (-3 * 7) + 0)),
(14, 2, 'DEMO: placa de esclava aportada por Alejandro Torres. Grabar "Alejandro". Acabado visual Oro; fuente Moderna; 25 x 25 mm; letra 2.0 mm. Forma: Placa circular. Revisar espacio en taller.', 400, 'ENTREGADO', 'Alejandro', 'Placa circular', 'Oro', 'Moderna', 25, 25, 2.0, (date_trunc('week',CURRENT_DATE)::date + (-3 * 7) + 1)),
(15, 3, 'DEMO: placa de dije aportada por Renata Aguilar. Grabar "Mi familia". Acabado visual Oro; fuente Moderna; 38 x 14 mm; letra 2.0 mm. Forma: Placa corazón. Revisar espacio en taller.', 300, 'ENTREGADO', 'Mi familia', 'Placa corazón', 'Oro', 'Moderna', 38, 14, 2.0, (date_trunc('week',CURRENT_DATE)::date + (-3 * 7) + 2)),
(16, 4, 'DEMO: argolla aportada por Santiago Molina. Grabar "Siempre juntos". Acabado visual Oro; fuente Moderna; diametro interior 17.0 mm y banda 4 mm; letra 1.5 mm. Revisar espacio en taller.', 500, 'ENTREGADO', 'Siempre juntos', 'Anillo', 'Oro', 'Moderna', 17.0, 4, 1.5, (date_trunc('week',CURRENT_DATE)::date + (-3 * 7) + 3)),
(17, 1, 'DEMO: anillo aportado por Paula Estrada. Grabar "P. E.". Acabado visual Plata; fuente Moderna; diametro interior 17.6 mm y banda 5 mm; letra 1.5 mm. Revisar espacio en taller.', 350, 'ENTREGADO', 'P. E.', 'Anillo', 'Plata', 'Moderna', 17.6, 5, 1.5, (date_trunc('week',CURRENT_DATE)::date + (-3 * 7) + 4)),
(18, 2, 'DEMO: placa de esclava aportada por Emilio Flores. Grabar "Emilio". Acabado visual Plata; fuente Moderna; 35 x 12 mm; letra 2.0 mm. Forma: Placa. Revisar espacio en taller.', 400, 'ENTREGADO', 'Emilio', 'Placa', 'Plata', 'Moderna', 35, 12, 2.0, (date_trunc('week',CURRENT_DATE)::date + (-3 * 7) + 5)),
(19, 3, 'DEMO: placa de dije aportada por Adriana Bermudez. Grabar "Mi familia". Acabado visual Plata; fuente Moderna; 38 x 14 mm; letra 2.0 mm. Forma: Placa ovalada. Revisar espacio en taller.', 300, 'ENTREGADO', 'Mi familia', 'Placa ovalada', 'Plata', 'Moderna', 38, 14, 2.0, (date_trunc('week',CURRENT_DATE)::date + (-2 * 7) + 0)),
(20, 4, 'DEMO: argolla aportada por David Vargas. Grabar "Siempre juntos". Acabado visual Plata; fuente Moderna; diametro interior 19.4 mm y banda 5 mm; letra 1.5 mm. Revisar espacio en taller.', 500, 'ENTREGADO', 'Siempre juntos', 'Anillo', 'Plata', 'Moderna', 19.4, 5, 1.5, (date_trunc('week',CURRENT_DATE)::date + (-2 * 7) + 1)),
(21, 1, 'DEMO: anillo aportado por Natalia Mendoza. Grabar "N. M.". Acabado visual Oro rosa; fuente Moderna; diametro interior 17.0 mm y banda 6 mm; letra 1.5 mm. Revisar espacio en taller.', 350, 'ENTREGADO', 'N. M.', 'Anillo', 'Oro rosa', 'Moderna', 17.0, 6, 1.5, (date_trunc('week',CURRENT_DATE)::date + (-2 * 7) + 2)),
(22, 2, 'DEMO: placa de esclava aportada por Joaquin Solis. Grabar "Joaquin". Acabado visual Oro rosa; fuente Moderna; 25 x 25 mm; letra 2.0 mm. Forma: Placa circular. Revisar espacio en taller.', 400, 'ENTREGADO', 'Joaquin', 'Placa circular', 'Oro rosa', 'Moderna', 25, 25, 2.0, (date_trunc('week',CURRENT_DATE)::date + (-2 * 7) + 3)),
(23, 3, 'DEMO: placa de dije aportada por Elena Rivas. Grabar "Mi familia". Acabado visual Oro rosa; fuente Moderna; 38 x 14 mm; letra 2.0 mm. Forma: Placa corazón. Revisar espacio en taller.', 300, 'ENTREGADO', 'Mi familia', 'Placa corazón', 'Oro rosa', 'Moderna', 38, 14, 2.0, (date_trunc('week',CURRENT_DATE)::date + (-2 * 7) + 4)),
(24, 4, 'DEMO: argolla aportada por Samuel Salazar. Grabar "Siempre juntos". Acabado visual Oro rosa; fuente Moderna; diametro interior 18.8 mm y banda 6 mm; letra 1.5 mm. Revisar espacio en taller.', 500, 'ENTREGADO', 'Siempre juntos', 'Anillo', 'Oro rosa', 'Moderna', 18.8, 6, 1.5, (date_trunc('week',CURRENT_DATE)::date + (-2 * 7) + 5)),
(25, 1, 'DEMO: anillo aportado por Carolina Vega. Grabar "C. V.". Acabado visual Oro; fuente Cursiva; diametro interior 19.4 mm y banda 4 mm; letra 1.5 mm. Revisar espacio en taller.', 425, 'ENTREGADO', 'C. V.', 'Anillo', 'Oro', 'Cursiva', 19.4, 4, 1.5, (date_trunc('week',CURRENT_DATE)::date + (-1 * 7) + 0)),
(26, 2, 'DEMO: placa de esclava aportada por Leonardo Duarte. Grabar "Leonardo". Acabado visual Oro; fuente Cursiva; 35 x 12 mm; letra 2.0 mm. Forma: Placa. Revisar espacio en taller.', 475, 'ENTREGADO', 'Leonardo', 'Placa', 'Oro', 'Cursiva', 35, 12, 2.0, (date_trunc('week',CURRENT_DATE)::date + (-1 * 7) + 1)),
(27, 3, 'DEMO: placa de dije aportada por Victoria Pineda. Grabar "Mi familia". Acabado visual Oro; fuente Cursiva; 38 x 14 mm; letra 2.0 mm. Forma: Placa ovalada. Revisar espacio en taller.', 375, 'ENTREGADO', 'Mi familia', 'Placa ovalada', 'Oro', 'Cursiva', 38, 14, 2.0, (date_trunc('week',CURRENT_DATE)::date + (-1 * 7) + 2)),
(28, 4, 'DEMO: argolla aportada por Esteban Larios. Grabar "Siempre juntos". Acabado visual Oro; fuente Cursiva; diametro interior 18.2 mm y banda 4 mm; letra 1.5 mm. Revisar espacio en taller.', 575, 'ENTREGADO', 'Siempre juntos', 'Anillo', 'Oro', 'Cursiva', 18.2, 4, 1.5, (date_trunc('week',CURRENT_DATE)::date + (-1 * 7) + 3)),
(29, 1, 'DEMO: anillo aportado por Amanda Castillo. Grabar "A. C.". Acabado visual Plata; fuente Cursiva; diametro interior 18.8 mm y banda 5 mm; letra 1.5 mm. Revisar espacio en taller.', 425, 'ENTREGADO', 'A. C.', 'Anillo', 'Plata', 'Cursiva', 18.8, 5, 1.5, (date_trunc('week',CURRENT_DATE)::date + (-1 * 7) + 4)),
(30, 2, 'DEMO: placa de esclava aportada por Rafael Morales. Grabar "Rafael". Acabado visual Plata; fuente Cursiva; 25 x 25 mm; letra 2.0 mm. Forma: Placa circular. Revisar espacio en taller.', 475, 'ENTREGADO', 'Rafael', 'Placa circular', 'Plata', 'Cursiva', 25, 25, 2.0, (date_trunc('week',CURRENT_DATE)::date + (-1 * 7) + 5)),
(31, 3, 'DEMO: placa de dije aportada por Valeria Flores. Grabar "Mi familia". Acabado visual Plata; fuente Cursiva; 38 x 14 mm; letra 2.0 mm. Forma: Placa corazón. Revisar espacio en taller.', 375, 'PENDIENTE', 'Mi familia', 'Placa corazón', 'Plata', 'Cursiva', 38, 14, 2.0, GREATEST(CURRENT_DATE, (date_trunc('week',CURRENT_DATE)::date + (0 * 7) + 0))),
(32, 4, 'DEMO: argolla aportada por Diego Bermudez. Grabar "Siempre juntos". Acabado visual Plata; fuente Cursiva; diametro interior 17.6 mm y banda 5 mm; letra 1.5 mm. Revisar espacio en taller.', 575, 'EN_PROCESO', 'Siempre juntos', 'Anillo', 'Plata', 'Cursiva', 17.6, 5, 1.5, GREATEST(CURRENT_DATE, (date_trunc('week',CURRENT_DATE)::date + (0 * 7) + 1))),
(33, 1, 'DEMO: anillo aportado por Camila Vargas. Grabar "C. V.". Acabado visual Oro rosa; fuente Cursiva; diametro interior 18.2 mm y banda 6 mm; letra 1.5 mm. Revisar espacio en taller.', 425, 'TERMINADO', 'C. V.', 'Anillo', 'Oro rosa', 'Cursiva', 18.2, 6, 1.5, GREATEST(CURRENT_DATE, (date_trunc('week',CURRENT_DATE)::date + (0 * 7) + 2))),
(34, 2, 'DEMO: placa de esclava aportada por Andres Mendoza. Grabar "Andres". Acabado visual Oro rosa; fuente Cursiva; 35 x 12 mm; letra 2.0 mm. Forma: Placa. Revisar espacio en taller. Cancelado por cambio de idea; no realizar.', 475, 'CANCELADO', 'Andres', 'Placa', 'Oro rosa', 'Cursiva', 35, 12, 2.0, GREATEST(CURRENT_DATE, (date_trunc('week',CURRENT_DATE)::date + (0 * 7) + 3))),
(35, 3, 'DEMO: placa de dije aportada por Daniela Solis. Grabar "Mi familia". Acabado visual Oro rosa; fuente Cursiva; 38 x 14 mm; letra 2.0 mm. Forma: Placa ovalada. Revisar espacio en taller.', 375, 'PENDIENTE', 'Mi familia', 'Placa ovalada', 'Oro rosa', 'Cursiva', 38, 14, 2.0, GREATEST(CURRENT_DATE, (date_trunc('week',CURRENT_DATE)::date + (0 * 7) + 4))),
(36, 4, 'DEMO: argolla aportada por Mateo Rivas. Grabar "Siempre juntos". Acabado visual Oro rosa; fuente Cursiva; diametro interior 17.0 mm y banda 6 mm; letra 1.5 mm. Revisar espacio en taller.', 575, 'EN_PROCESO', 'Siempre juntos', 'Anillo', 'Oro rosa', 'Cursiva', 17.0, 6, 1.5, GREATEST(CURRENT_DATE, (date_trunc('week',CURRENT_DATE)::date + (0 * 7) + 5))),
(37, 1, 'DEMO: anillo aportado por Lucia Salazar. Grabar "L. S.". Acabado visual Oro; fuente Clásica; diametro interior 17.6 mm y banda 4 mm; letra 1.5 mm. Revisar espacio en taller.', 350, 'PENDIENTE', 'L. S.', 'Anillo', 'Oro', 'Clásica', 17.6, 4, 1.5, GREATEST(CURRENT_DATE, (date_trunc('week',CURRENT_DATE)::date + (1 * 7) + 0))),
(38, 2, 'DEMO: placa de esclava aportada por Gabriel Vega. Grabar "Gabriel". Acabado visual Oro; fuente Clásica; 25 x 25 mm; letra 2.0 mm. Forma: Placa circular. Revisar espacio en taller.', 400, 'EN_PROCESO', 'Gabriel', 'Placa circular', 'Oro', 'Clásica', 25, 25, 2.0, GREATEST(CURRENT_DATE, (date_trunc('week',CURRENT_DATE)::date + (1 * 7) + 1))),
(39, 3, 'DEMO: placa de dije aportada por Sofia Duarte. Grabar "Mi familia". Acabado visual Oro; fuente Clásica; 38 x 14 mm; letra 2.0 mm. Forma: Placa corazón. Revisar espacio en taller.', 300, 'TERMINADO', 'Mi familia', 'Placa corazón', 'Oro', 'Clásica', 38, 14, 2.0, GREATEST(CURRENT_DATE, (date_trunc('week',CURRENT_DATE)::date + (1 * 7) + 2))),
(40, 4, 'DEMO: argolla aportada por Nicolas Pineda. Grabar "Siempre juntos". Acabado visual Oro; fuente Clásica; diametro interior 19.4 mm y banda 4 mm; letra 1.5 mm. Revisar espacio en taller. Cancelado por cambio de idea; no realizar.', 500, 'CANCELADO', 'Siempre juntos', 'Anillo', 'Oro', 'Clásica', 19.4, 4, 1.5, GREATEST(CURRENT_DATE, (date_trunc('week',CURRENT_DATE)::date + (1 * 7) + 3))),
(41, 1, 'DEMO: anillo aportado por Mariana Larios. Grabar "M. L.". Acabado visual Plata; fuente Clásica; diametro interior 17.0 mm y banda 5 mm; letra 1.5 mm. Revisar espacio en taller.', 350, 'PENDIENTE', 'M. L.', 'Anillo', 'Plata', 'Clásica', 17.0, 5, 1.5, GREATEST(CURRENT_DATE, (date_trunc('week',CURRENT_DATE)::date + (1 * 7) + 4))),
(42, 2, 'DEMO: placa de esclava aportada por Sebastian Castillo. Grabar "Sebastian". Acabado visual Plata; fuente Clásica; 35 x 12 mm; letra 2.0 mm. Forma: Placa. Revisar espacio en taller.', 400, 'EN_PROCESO', 'Sebastian', 'Placa', 'Plata', 'Clásica', 35, 12, 2.0, GREATEST(CURRENT_DATE, (date_trunc('week',CURRENT_DATE)::date + (1 * 7) + 5))),
(43, 3, 'DEMO: placa de dije aportada por Isabella Morales. Grabar "Mi familia". Acabado visual Plata; fuente Clásica; 38 x 14 mm; letra 2.0 mm. Forma: Placa ovalada. Revisar espacio en taller.', 300, 'PENDIENTE', 'Mi familia', 'Placa ovalada', 'Plata', 'Clásica', 38, 14, 2.0, GREATEST(CURRENT_DATE, (date_trunc('week',CURRENT_DATE)::date + (2 * 7) + 0))),
(44, 4, 'DEMO: argolla aportada por Alejandro Zamora. Grabar "Siempre juntos". Acabado visual Plata; fuente Clásica; diametro interior 18.8 mm y banda 5 mm; letra 1.5 mm. Revisar espacio en taller.', 500, 'EN_PROCESO', 'Siempre juntos', 'Anillo', 'Plata', 'Clásica', 18.8, 5, 1.5, GREATEST(CURRENT_DATE, (date_trunc('week',CURRENT_DATE)::date + (2 * 7) + 1))),
(45, 1, 'DEMO: anillo aportado por Renata Reyes. Grabar "R. R.". Acabado visual Oro rosa; fuente Clásica; diametro interior 19.4 mm y banda 6 mm; letra 1.5 mm. Revisar espacio en taller.', 350, 'TERMINADO', 'R. R.', 'Anillo', 'Oro rosa', 'Clásica', 19.4, 6, 1.5, GREATEST(CURRENT_DATE, (date_trunc('week',CURRENT_DATE)::date + (2 * 7) + 2))),
(46, 2, 'DEMO: placa de esclava aportada por Santiago Navarro. Grabar "Santiago". Acabado visual Oro rosa; fuente Clásica; 25 x 25 mm; letra 2.0 mm. Forma: Placa circular. Revisar espacio en taller. Cancelado por cambio de idea; no realizar.', 400, 'CANCELADO', 'Santiago', 'Placa circular', 'Oro rosa', 'Clásica', 25, 25, 2.0, GREATEST(CURRENT_DATE, (date_trunc('week',CURRENT_DATE)::date + (2 * 7) + 3))),
(47, 3, 'DEMO: placa de dije aportada por Paula Torres. Grabar "Mi familia". Acabado visual Oro rosa; fuente Clásica; 38 x 14 mm; letra 2.0 mm. Forma: Placa corazón. Revisar espacio en taller.', 300, 'PENDIENTE', 'Mi familia', 'Placa corazón', 'Oro rosa', 'Clásica', 38, 14, 2.0, GREATEST(CURRENT_DATE, (date_trunc('week',CURRENT_DATE)::date + (2 * 7) + 4))),
(48, 4, 'DEMO: argolla aportada por Emilio Aguilar. Grabar "Siempre juntos". Acabado visual Oro rosa; fuente Clásica; diametro interior 18.2 mm y banda 6 mm; letra 1.5 mm. Revisar espacio en taller.', 500, 'EN_PROCESO', 'Siempre juntos', 'Anillo', 'Oro rosa', 'Clásica', 18.2, 6, 1.5, GREATEST(CURRENT_DATE, (date_trunc('week',CURRENT_DATE)::date + (2 * 7) + 5))),
(49, 5, 'DEMO: Ajustar anillo de plata aportado por cliente de talla 7 a talla 8; confirmar medida con anillero. Sin grabado.', 450, 'PENDIENTE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, GREATEST(CURRENT_DATE, (date_trunc('week',CURRENT_DATE)::date + (3 * 7) + 0))),
(50, 6, 'DEMO: Reemplazar broche de cadena de plata aportada por cliente; comprobar cierre y uniones. Sin grabado.', 500, 'EN_PROCESO', NULL, NULL, NULL, NULL, NULL, NULL, NULL, GREATEST(CURRENT_DATE, (date_trunc('week',CURRENT_DATE)::date + (3 * 7) + 1))),
(51, 7, 'DEMO: Limpiar y pulir pulsera de oro aportada por cliente; conservar marcas existentes. Sin grabado.', 250, 'TERMINADO', NULL, NULL, NULL, NULL, NULL, NULL, NULL, GREATEST(CURRENT_DATE, (date_trunc('week',CURRENT_DATE)::date + (3 * 7) + 2))),
(52, 8, 'DEMO: Revisar engaste de anillo aportado por cliente; asegurar piedra existente. Sin grabado ni piedra nueva. Cancelado; no realizar.', 600, 'CANCELADO', NULL, NULL, NULL, NULL, NULL, NULL, NULL, GREATEST(CURRENT_DATE, (date_trunc('week',CURRENT_DATE)::date + (3 * 7) + 3))),
(53, 5, 'DEMO: Ajustar anillo de plata aportado por cliente de talla 7 a talla 8; confirmar medida con anillero. Sin grabado.', 450, 'PENDIENTE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, GREATEST(CURRENT_DATE, (date_trunc('week',CURRENT_DATE)::date + (3 * 7) + 4))),
(54, 6, 'DEMO: Reemplazar broche de cadena de plata aportada por cliente; comprobar cierre y uniones. Sin grabado.', 500, 'EN_PROCESO', NULL, NULL, NULL, NULL, NULL, NULL, NULL, GREATEST(CURRENT_DATE, (date_trunc('week',CURRENT_DATE)::date + (3 * 7) + 5))),
(55, 7, 'DEMO: Limpiar y pulir pulsera de oro aportada por cliente; conservar marcas existentes. Sin grabado.', 250, 'PENDIENTE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, GREATEST(CURRENT_DATE, (date_trunc('week',CURRENT_DATE)::date + (4 * 7) + 0))),
(56, 8, 'DEMO: Revisar engaste de anillo aportado por cliente; asegurar piedra existente. Sin grabado ni piedra nueva.', 600, 'EN_PROCESO', NULL, NULL, NULL, NULL, NULL, NULL, NULL, GREATEST(CURRENT_DATE, (date_trunc('week',CURRENT_DATE)::date + (4 * 7) + 1))),
(57, 5, 'DEMO: Ajustar anillo de plata aportado por cliente de talla 7 a talla 8; confirmar medida con anillero. Sin grabado.', 450, 'TERMINADO', NULL, NULL, NULL, NULL, NULL, NULL, NULL, GREATEST(CURRENT_DATE, (date_trunc('week',CURRENT_DATE)::date + (4 * 7) + 2))),
(58, 6, 'DEMO: Reemplazar broche de cadena de plata aportada por cliente; comprobar cierre y uniones. Sin grabado. Cancelado; no realizar.', 500, 'CANCELADO', NULL, NULL, NULL, NULL, NULL, NULL, NULL, GREATEST(CURRENT_DATE, (date_trunc('week',CURRENT_DATE)::date + (4 * 7) + 3))),
(59, 7, 'DEMO: Limpiar y pulir pulsera de oro aportada por cliente; conservar marcas existentes. Sin grabado.', 250, 'PENDIENTE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, GREATEST(CURRENT_DATE, (date_trunc('week',CURRENT_DATE)::date + (4 * 7) + 4))),
(60, 8, 'DEMO: Revisar engaste de anillo aportado por cliente; asegurar piedra existente. Sin grabado ni piedra nueva.', 600, 'EN_PROCESO', NULL, NULL, NULL, NULL, NULL, NULL, NULL, GREATEST(CURRENT_DATE, (date_trunc('week',CURRENT_DATE)::date + (4 * 7) + 5)));
COMMIT;
