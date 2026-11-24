# Defensa de Joyería Quilate

**Equipo:** Kate Ramirez, Erick Arana, Reynolds Rodriguez y Francisco Cuadra.

Quilate reúne inventario, ventas, clientes, apartados y pedidos personalizados de una joyería. La demostración recorre una operación de tienda, su persistencia y los reportes que permiten revisar el negocio.

## Preparación en IntelliJ IDEA

1. Abrir el proyecto desde `pom.xml`, seleccionar JDK 21 y recargar Maven. Preparar la base de demostración siguiendo el [README](../../README.md). Si la consigna exige MySQL, activar el perfil Maven `mysql` y seguir los pasos de [MySQL](../../database/mysql/README.md).
2. Ejecutar `com.quilate.Launcher` desde IntelliJ y esperar «Conectado a la base de datos». Abrir Inventario y Ventas antes de la exposición para comprobar que hay registros en ambas pantallas.
3. Reservar el código `DEF-001` para una pieza temporal. Abrir una carpeta local donde guardar los dos PDF en vivo. Los [archivos de muestra](reportes/) contienen datos ficticios y sirven para revisar el formato.
4. En Jaspersoft Studio, abrir `inventario_general.jrxml` y `ventas_por_fechas.jrxml` desde [los reportes del proyecto](../../src/main/resources/reportes/). Asociar un adaptador JDBC a la base de demostración. Revisar **Design** y **Preview**, incluidas las consultas y los campos de título, fecha y detalle. Para Ventas, introducir un intervalo que incluya la venta de demostración y emplear como límite superior el inicio del día siguiente. Llevar evidencia de este paso para acreditar el uso de Studio.
5. Cada integrante localiza un commit o PR propio que pueda explicar. Comparar la rama local con la rama que se mostrará durante la defensa. El [repositorio configurado](https://github.com/kateV1013/Persistencia-de-datos-con-Hibernate-ORM) y el historial local son las referencias para preparar esa parte.

## Guion sugerido de 10 minutos

| Tiempo | Integrante | Qué muestra y explica |
|---|---|---|
| 0:00–2:00 | **Kate Ramirez** | Abre el proyecto en IntelliJ, ejecuta el lanzador y presenta el problema de la joyería. Recorre Tienda y Taller. En Inventario, intenta guardar con un dato obligatorio vacío y muestra el mensaje. Explica cómo se conectan FXML y los controladores. |
| 2:00–5:00 | **Reynolds Rodriguez** | Crea la pieza `DEF-001` con costo C$ 900, precio C$ 1,800 y 3 existencias. La busca por código, actualiza a 4 existencias y reinicia la aplicación para comprobar que conserva el cambio. Después elimina esa pieza, que debe seguir sin ventas ni apartados asociados, y comprueba que la búsqueda ya no devuelve el registro. Explica la relación entre pieza y categoría. |
| 5:00–8:00 | **Erick Arana** | Abre **Inventario → Inventario general** y **Ventas → Ventas por fechas**. Selecciona un intervalo con ventas confirmadas. Explica que JasperReports llena las plantillas con la conexión de Hibernate. En cada visor pulsa **Guardar**, elige **PDF**, guarda y abre el archivo. Señala el título, los encabezados y la fecha de emisión, y coteja una fila y su importe con JavaFX. |
| 8:00–10:00 | **Francisco Cuadra** | Muestra un pedido personalizado y explica cómo se vincula con cliente y tipo de servicio. Presenta la estructura del proyecto, el README y el historial de Git. Da paso a cada integrante para explicar un commit o PR propio. |

Estos turnos son una propuesta para la exposición. Cada integrante debe atribuirse únicamente el trabajo que pueda identificar en el historial.

## Preguntas para el ensayo

- **Kate.** ¿Qué hace un controlador cuando el usuario deja un campo obligatorio vacío? ¿Qué responsabilidades tiene FXML?
- **Reynolds.** ¿Cómo se comprueba que la edición quedó en la base? ¿Por qué no se puede eliminar una pieza que ya se vendió?
- **Erick.** ¿Qué diferencia hay entre `.jrxml`, `.jasper` y PDF? ¿Cómo se aplica el intervalo de fechas?
- **Francisco.** ¿Dónde se implementan las reglas de pedidos? ¿Cómo se identifica la contribución de cada persona en Git?