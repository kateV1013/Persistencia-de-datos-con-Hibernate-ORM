# Quilate · Burdeos, marfil y dorado

El menú agrupa los módulos de la tienda y del taller y ofrece acceso directo
a Ventas. La [referencia visual del menú](docs/diseno/referencia-menu-principal.png)
documenta su composición.

## Paleta

| Color | Código | Uso |
|---|---|---|
| Burdeos | `#5D111B` | Marca, títulos y acciones principales |
| Dorado | `#D1AB74` | Acentos y foco de teclado |
| Marfil | `#F8F0E5` | Fondo de las ventanas |
| Crema claro | `#FFFDF9` | Superficies y formularios |
| Marrón | `#392425` | Texto principal |
| Verde | `#14734B` | Confirmaciones y conexión activa |
| Rojo | `#A43F42` | Acciones destructivas y errores |

Open Sans se incluye con su licencia. Los títulos y la marca usan fuentes
serif. El menú combina el logotipo vectorial con una imagen del anillo de portada.

## Navegación y módulos

- Inicio con pestañas de navegación, búsqueda sin distinción de mayúsculas o tildes y acceso directo a ventas.
- Ventanas de trabajo con encabezado y colores coherentes.
- Clientes, apartados e inventario con tablas a la izquierda y fichas de edición a la derecha.
- Categorías agrupadas en un panel desplegable dentro de Inventario.
- Formularios con etiquetas permanentes y botones diferenciados para guardar, actualizar, confirmar y eliminar.
- Estados en etiquetas de color, selector de moneda e importes y fechas legibles.
- Seis vistas de análisis: piezas, días, horarios, mejores clientes, fechas y descuentos con ganancia bruta.
- Perfil del cliente con frecuencia por día y hora, compras y monto acumulado.
- Lectura por código de barras en Ventas: Enter agrega una unidad, agrupa la misma pieza y valida existencias.
- Resumen del carrito con el descuento aplicado y el total de la venta.
- Formularios con encabezados, iconos de acciones y campos de estilo uniforme en todos los módulos.
- Atelier con vista previa interactiva de grabados en anillos y placas, tres acabados, tres tipografías y medidas en milímetros. Los parámetros se guardan con el pedido.
- Formularios desplazables y tablas con desplazamiento horizontal cuando no cabe el ancho mínimo legible.

Los módulos siguen abriéndose en ventanas independientes. Volver a abrir uno existente trae su ventana al frente.

## Ejecución y comprobación

Sigue las [instrucciones de instalación y arranque](README.md#2-instalación-y-ejecución).
Las capturas de `docs/diseno/` usan datos ficticios.

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
- `src/main/java/com/quilate/ui/MarcaQuilate.java`: logotipo vectorial.
- `src/main/resources/imagenes/anillo-portada.png`: imagen de portada.
- `src/main/java/com/quilate/ui/Ventanas.java`: ventanas y carga de fuentes.

## Reglas del análisis y del editor

Los reportes incluyen solo ventas confirmadas. El rango inicial cubre los últimos 30 días y sus extremos son inclusivos. Las estadísticas y la ganancia usan los importes históricos del detalle de venta. La ganancia bruta no descuenta gastos operativos. Al confirmar una venta nueva se registra ese momento, en lugar del momento en que se creó el borrador.

El descuento máximo sin pérdida es precio menos costo por unidad. Exactamente en ese límite no hay ganancia bruta. Los descuentos superiores continúan bloqueados tanto al calcular como al confirmar.

La vista previa del Atelier es orientativa. Representa proporciones y textos; no es un plano de fabricación, no se muestra a tamaño real ni convierte milímetros a tallas comerciales. Aplicar al pedido conserva los parámetros en el formulario; Guardar persiste el pedido y su diseño. Volver cierra el editor sin aplicar cambios.
