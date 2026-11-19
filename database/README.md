# Base de datos · Joyería Quilate

## Instalación nueva

Ejecuta en una base PostgreSQL vacía, en este orden:

1. 01_quilate_bd_inicial.sql: tablas base y categorías.
2. 02_pedidos_y_estado_venta.sql: servicios y pedidos.
3. 03_diseno_personalizado.sql: campos del diseño personalizado.
4. 04_reemplazar_datos_demo.sql: datos ficticios para practicar.

Los scripts 01 y 02 ya no insertan los clientes, piezas ni servicios anteriores.
El nombre técnico de la base y la configuración de conexión se mantienen.

## Reemplazar datos existentes

Cierra la aplicación y respalda tu base con pgAdmin o pg_dump. Comprueba que
las tres migraciones estén aplicadas y ejecuta el archivo 04 completo en
la consola SQL de esa base. **Borra y reemplaza todos los clientes, piezas,
categorías, ventas, detalles, apartados, servicios y pedidos**, incluidos los
diseños guardados de los pedidos. No se ejecuta automáticamente al abrir la app.

El reemplazo es una sola transacción; si falla, ejecuta ROLLBACK en la misma
consola antes de reintentar. No usa CASCADE: una dependencia externa impide
el borrado. Repetirlo descarta lo agregado desde la carga anterior.

## Contenido ficticio ampliado

- 5 categorías y 64 piezas: 16 modelos base y 48 variantes por talla o medida.
- 60 clientes con correos example.com y sin teléfonos personales.
- 120 ventas: 100 confirmadas, 10 anuladas y 10 en preparación; 234 detalles.
- 24 apartados: 12 activos, 6 cancelados y 6 completados vinculados a sus ventas.
- 8 servicios de ejemplo y 60 pedidos: 48 grabados y 12 mantenimientos.

Todos los datos e importes son ficticios y están en cordobas (C$).
Los clientes se registran antes de sus movimientos. Hay ventas de los últimos
90 días, descuentos del 5% en algunas operaciones y distintos métodos de pago.
El stock parte de 40 unidades por pieza y descuenta solo ventas confirmadas y
apartados activos. Los apartados completados se cuentan una vez, en su venta;
las ventas anuladas tienen el stock reintegrado.

### Personalización coherente con el editor

Los 48 grabados guardan texto, forma, acabado, fuente, medida, ancho y altura
de letra. Se pueden abrir en **Pedidos personalizados → Diseñar pieza**.

- 24 anillos: iniciales o dedicatorias interiores; diámetro de 17 a 19,4 mm,
  banda de 4 a 6 mm y letra de 1,5 mm.
- 24 placas para esclavas o dijes: nombres o dedicatorias; rectangulares, ovaladas, circulares y de corazón. Las circulares miden
  25 × 25 mm; las otras, 35 × 12 o 38 × 14 mm, con letra de 2 mm.
- Acabados Oro, Plata y Oro rosa; fuentes Clásica, Moderna y Cursiva.
- El texto de cada descripción coincide con el grabado y sus medidas.
- Los precios cubren únicamente el trabajo sobre una pieza aportada por el
  cliente: precio base del servicio, más C$75 por cursiva y C$50 si el texto
  supera 10 caracteres. No se descuenta inventario por esas piezas externas.
- Los 12 ajustes, reparaciones, limpiezas y revisiones no tienen un grabado
  ficticio asociado: sus siete columnas de diseño quedan vacías.

Los pedidos se distribuyen en 10 semanas, 6 por semana, sin superar el cupo
de 20 pedidos no cancelados que aplica el programa. Los entregados tienen
fechas pasadas; los pendientes, en proceso y terminados, fechas actuales o
futuras. Si se carga a mitad de semana, los trabajos de esa semana se
programan desde hoy. El taller debe validar el espacio físico antes de grabar.

### Ejemplos para probar

- Pedido 1: iniciales V. M., anillo en oro, fuente Clásica.
- Pedido 14: nombre Alejandro, placa de esclava, oro y fuente Moderna.
- Pedido 25: iniciales C. V., anillo en oro, fuente Cursiva.
- Pedidos 31 a 48: grabados actuales o próximos, con distintos estados.
- Pedidos 49 a 60: mantenimiento sin vista previa de grabado.

Busca Mujer, Hombre o Unisex en inventario para explorar el catálogo por
público; son etiquetas dentro del nombre, no una columna nueva.

Consulta la descripción del proyecto en ../docs/JOYERIA_QUILATE.md.
