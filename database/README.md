# Base de datos

Ejecutar los scripts sobre una base PostgreSQL vacia, en este orden:

1. `01_quilate_bd_inicial.sql`
2. `02_pedidos_y_estado_venta.sql`

El primer script crea inventario, clientes, apartados, ventas y detalles. El segundo agrega
tipos de servicio y pedidos personalizados. Son idempotentes y no contienen credenciales.
