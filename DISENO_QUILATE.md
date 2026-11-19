# Quilate · Diseño esmeralda y champán

La interfaz combina la identidad de una joyería con la claridad de una aplicación de gestión. El rediseño abarca el inicio y los siete módulos.

## Paleta

| Color | Código | Uso |
|---|---|---|
| Esmeralda profundo | `#153D34` | Marca, encabezados y acciones principales |
| Esmeralda suave | `#245C4E` | Confirmaciones, interacción y gráficos |
| Dorado champán | `#D4B778` | Identidad, llamada principal y foco de teclado |
| Marfil | `#F5F4EF` | Fondo de las ventanas |
| Blanco | `#FFFFFF` | Formularios, tarjetas y tablas |
| Gris verdoso | `#65736C` | Texto secundario |
| Rojo suave | `#A43F42` | Acciones destructivas y errores |

Open Sans se incluye en el proyecto, junto con su licencia, para mantener una apariencia consistente. Los títulos editoriales usan la familia lógica Serif de JavaFX. Los iconos y la ilustración del diamante se dibujan con vectores.

## Qué cambia

- Inicio con navegación lateral, tarjetas de los siete módulos, búsqueda sin distinción de mayúsculas o tildes y acceso directo a ventas.
- Ventanas de trabajo con encabezado y colores coherentes.
- Clientes, apartados e inventario con tablas a la izquierda y fichas de edición a la derecha.
- Categorías agrupadas en un panel desplegable dentro de Inventario.
- Formularios con etiquetas permanentes y botones diferenciados para guardar, actualizar, confirmar y eliminar.
- Estados en etiquetas de color, importes en córdobas y fechas legibles.
- Seis vistas de análisis: piezas, días, horarios, mejores clientes, fechas y descuentos con ganancia bruta.
- Perfil del cliente con frecuencia por día y hora, compras y monto acumulado.
- Lectura por código de barras en Ventas: Enter agrega una unidad, agrupa la misma pieza y valida existencias.
- Resumen del carrito con descuentos, ganancia bruta y porcentaje de margen.
- Formularios con encabezados, iconos de acciones y campos de estilo uniforme en todos los módulos.
- Atelier con vista previa interactiva de grabados en anillos y placas, tres acabados, tres tipografías y medidas en milímetros. Los parámetros se guardan con el pedido.
- Formularios desplazables y tablas con desplazamiento horizontal cuando no cabe el ancho mínimo legible.
- La navegación lateral se oculta al reducir el inicio; los módulos continúan disponibles mediante sus tarjetas.

Los módulos siguen abriéndose en ventanas independientes. Volver a abrir uno existente trae su ventana al frente.

## Abrir en IntelliJ IDEA

1. Extrae el ZIP y abre la carpeta que contiene `pom.xml`.
2. Selecciona un **JDK 21 completo** como SDK del proyecto y como JDK de Maven.
3. Recarga el proyecto Maven.
4. Ejecuta `database/03_diseno_personalizado.sql` en tu base existente desde pgAdmin. Para una base nueva, ejecuta `01`, `02` y `03` en orden.
5. Usa tu configuración existente de PostgreSQL en `db.properties` o las variables de entorno indicadas en el README original.
6. Ejecuta `com.quilate.Launcher`, o desde PowerShell:

```powershell
.\mvnw.cmd clean javafx:run
```

La base de datos y las credenciales de tu equipo siguen siendo necesarias para trabajar con tus datos. No se necesita instalar una biblioteca de temas ni descargar fuentes para mostrar el diseño.

## Comprobación

Se compiló con Java 21 y se ejecutó la suite de 40 pruebas sin fallos. Incluye consultas históricas con filtros de fecha, conteo sin duplicar ventas, cálculo de ganancias y descuentos, códigos con ceros iniciales, límite de existencias, guardado/modificación/retiro de diseños y navegación de JavaFX. La prueba visual interactúa con el lector y el editor, y recorre las seis pestañas de reportes. Se revisaron capturas reales de las vistas y variantes compactas. Después del ajuste final de fechas y perfiles se repitieron las dos pruebas de interfaz, también sin fallos.

Las pruebas de persistencia usaron H2 en memoria. No se verificó una conexión a la instancia PostgreSQL de tu computadora. Las capturas de `docs/diseno/` muestran datos ficticios que solo existen en esa base de prueba; no se insertan al ejecutar la aplicación normalmente.

Para ejecutar las pruebas en un equipo con entorno gráfico:

```powershell
.\mvnw.cmd test
```

Para regenerar las capturas de diseño con datos ficticios en H2:

```powershell
.\mvnw.cmd "-Dtest=DisenoVisualTest" "-Dquilate.capturas=true" test
```

Las nuevas capturas se guardan en `target/diseno/`.

## Archivos principales del diseño

- `src/main/resources/css/styles.css`: paleta y estilos compartidos.
- `src/main/resources/fxml/`: estructura de las ocho vistas.
- `src/main/resources/fonts/`: fuentes incluidas y licencia.
- `src/main/java/com/quilate/controller/MainController.java`: navegación y búsqueda.
- `src/main/java/com/quilate/ui/PresentacionTablas.java`: formatos y adaptación de tablas.
- `src/main/java/com/quilate/ui/DiamanteEditorial.java`: ilustración vectorial.
- `src/main/java/com/quilate/ui/Ventanas.java`: ventanas y carga de fuentes.

La arquitectura JavaFX / Hibernate y las operaciones de negocio del proyecto se conservan. No se agregaron dependencias al POM para el diseño.

## Reglas del análisis y del editor

Los reportes incluyen solo ventas confirmadas. El rango inicial cubre los últimos 30 días y sus extremos son inclusivos. Las estadísticas y la ganancia usan los importes históricos del detalle de venta. La ganancia bruta no descuenta gastos operativos. Al confirmar una venta nueva se registra ese momento, en lugar del momento en que se creó el borrador.

El descuento máximo sin pérdida es precio menos costo por unidad. Exactamente en ese límite no hay ganancia bruta. Los descuentos superiores continúan bloqueados tanto al calcular como al confirmar.

La vista previa del Atelier es orientativa. Representa proporciones y textos; no es un plano de fabricación, no se muestra a tamaño real ni convierte milímetros a tallas comerciales. Aplicar al pedido conserva los parámetros en el formulario; Guardar persiste el pedido y su diseño. Volver cierra el editor sin aplicar cambios.
