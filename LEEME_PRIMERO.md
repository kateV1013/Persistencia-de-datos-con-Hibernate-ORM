# Quilate · Nueva versión

## Antes de abrirla

Si ya usabas Quilate, abre **pgAdmin**, selecciona tu base de datos de Quilate y ejecuta una vez el archivo **`database/03_diseno_personalizado.sql`**. Agrega las columnas que guardan el grabado y las medidas; conserva tus registros y se puede repetir. No borres ni recrees tu base.

Para una instalación desde cero, ejecuta los scripts `01`, `02` y `03`, en ese orden, sobre tu base PostgreSQL.

Después abre la carpeta que contiene `pom.xml` en IntelliJ, selecciona **JDK 21**, recarga Maven y ejecuta `com.quilate.Launcher`. Conserva tu configuración de conexión en `db.properties` o las variables indicadas en README.

```powershell
.\mvnw.cmd clean javafx:run
```

## Recorrido para presentar el proyecto

1. **Inventario:** registra una pieza con código de barras único, costo, precio y existencias. Puedes escribir o escanear el código; sus ceros iniciales se conservan.
2. **Ventas:** enfoca el campo del lector, escanea y pulsa Enter. Un lector USB configurado como teclado con sufijo Enter lo hace automáticamente. Cada lectura añade una unidad. Selecciona la línea para cambiar cantidad o descuento y pulsa Agregar pieza. Selecciona el cliente y confirma.
3. **Límite de descuento:** precio menos costo por unidad. Si se supera, se bloquea y se indica el máximo. Aplicar exactamente el máximo deja ganancia bruta cero; por eso se distingue «Sin ganancia» de «Con ganancia».
4. **Clientes:** selecciona uno y revisa su historial, monto comprado, días y franjas horarias más frecuentes. La frecuencia se calcula por ventas confirmadas, mostrando los empates.
5. **Reportes y análisis:** elige fechas o Todo el historial. Recorre Piezas, Días de semana, Horarios, Mejores clientes, Fechas y Descuentos y margen. Los clientes se ordenan por monto comprado. Las ventas anuladas y los borradores no participan.
6. **Pedidos personalizados:** completa los datos y abre Diseñar pieza. Elige anillo o placa, acabado visual, texto, tipografía y medidas. Pulsa Aplicar al pedido y después **Guardar**. Al seleccionar el pedido de nuevo, su diseño se recupera.

El editor es una ilustración interactiva orientativa, sin escala física real. Las medidas se expresan en milímetros; el diámetro interior no equivale a una talla comercial. El taller valida el ajuste y el espacio del grabado. El acabado visual no cambia automáticamente el precio ni las existencias.

La ganancia mostrada es **bruta**, calculada con el costo histórico y después del descuento. No incluye gastos operativos. Las nuevas ventas guardan la fecha y hora de confirmación según el reloj del equipo; las ventas antiguas conservan su fecha existente.

Los datos de las capturas de `docs/diseno/` son ficticios. Las pruebas usan H2 y no modifican tu PostgreSQL. La conexión a tu instancia se comprueba al abrir el proyecto en tu computadora.
