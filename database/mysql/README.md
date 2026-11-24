# Ejecutar Quilate con MySQL

Esta configuración apunta a **MySQL 8.4** mediante el perfil Maven `mysql`. Usá una base independiente para la demostración. El proyecto conserva el arranque habitual con PostgreSQL.

1. Crear una base vacía, por ejemplo `CREATE DATABASE quilate_demo CHARACTER SET utf8mb4;`, y seleccionarla con `USE quilate_demo;`.
2. Ejecutar [01_esquema.sql](01_esquema.sql) para crear las ocho tablas y [02_datos_demo.sql](02_datos_demo.sql) una vez para cargar una pieza, una venta confirmada, un apartado y un pedido de prueba.
3. En la configuración de ejecución de IntelliJ, definir `DB_URL=jdbc:mysql://localhost:3306/quilate_demo`, `DB_USER`, `DB_PASSWORD` y `DB_SCHEMA_MODE=validate`. Trabajar desde la carpeta del proyecto. Las variables de entorno tienen prioridad sobre `db.properties`.
4. Activar el perfil `mysql` en la ventana Maven de IntelliJ, recargar dependencias y ejecutar `com.quilate.Launcher`. En la terminal, con esas variables definidas, el comando equivalente es `.\mvnw.cmd -Pmysql javafx:run`.
5. Confirmar que Inventario muestra `DEMO-001` y que **Ventas por fechas** incluye la venta de Ana Demo por **C$ 1,800.00** al consultar el día de carga.

La comprobación con un servidor MySQL requiere ejecutar estos pasos en un equipo con esa versión instalada.