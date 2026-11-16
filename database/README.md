# Base de datos

## Actualizar una instalación existente

Ejecuta `03_diseno_personalizado.sql` en la base PostgreSQL que ya usa Quilate, antes de iniciar esta versión. Añade siete columnas opcionales a `pedido_personalizado` para guardar la vista previa. Conserva todos los datos y se puede repetir.

## Instalación nueva

Ejecuta sobre tu base PostgreSQL vacía, en este orden:

1. `01_quilate_bd_inicial.sql`
2. `02_pedidos_y_estado_venta.sql`
3. `03_diseno_personalizado.sql`

El primer script crea inventario, clientes, apartados, ventas y detalles. El segundo agrega tipos de servicio y pedidos personalizados. El tercero agrega texto, forma, acabado, tipografía y medidas del diseño. No contienen credenciales.
