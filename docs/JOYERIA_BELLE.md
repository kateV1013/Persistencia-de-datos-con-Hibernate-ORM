# Joyeria Belle · adaptación del proyecto

Consulta pública: 16 de noviembre de 2026.

El nombre, la ciudad (Managua, Nicaragua), la atención a mujeres y hombres y
la referencia del monograma JB fueron proporcionados por el usuario.
MarcaBelle.java contiene una adaptación vectorial: círculo marfil (#E8E2DC) y monograma rosa empolvado (#BA9A90). No es una extracción exacta del archivo original.
La interfaz se adapta al logo: rosa empolvado, marfil, fondos taupe y texto marrón oscuro. Se conservan los colores semánticos de éxito y error y los metales de las vistas previas de joyas.

## Referencias encontradas

- [FindGlocal, joyerías de Managua](https://www.findglocal.com/NI/Managua/1474661-24/genre/188031587886173/Joyer%C3%ADas):
  publica Belle en Plaza Fontana, segundo piso, y menciona oro 14–18K y
  plata 925. Indica lunes a viernes de 10 a 18 y sábado de 10 a 17.
- [Ficha Waze de Joyería Belle](https://www.waze.com/live-map/directions/ni/mn/managua/joyeria-belle?to=place.ChIJ9a8e4PBVcY8RITwtsL7uCDs):
  ubica un negocio con ese nombre en Managua, con horario de lunes a
  sábado de 9 a 18.

Son directorios de terceros, no confirmación directa del negocio.
Los horarios discrepan; dirección, horario y contacto requieren confirmación
antes de publicarlos como vigentes. No se identificó una cuenta oficial
inequívoca en los resultados consultados. Se descartaron homónimos de otros países.

## Aplicación al proyecto

El catálogo ficticio incluye oro 14/18K y plata 925 como contexto de demostración,
sin afirmar que Belle venda esos modelos ni a esos precios. Grabado, reparación,
ajuste y limpieza son ejemplos académicos, no servicios confirmados.

Los módulos existentes permiten practicar ventas, inventario, clientes,
apartados, servicios, pedidos personalizados y reportes. Para ampliar el
proyecto con datos reales hace falta confirmar catálogo, política de apartados
y servicios directamente con la joyería. No se incorporaron horarios, teléfonos
ni direcciones sin confirmar a la interfaz.

Los datos antiguos se retiraron de los scripts iniciales. El archivo
database/04_reemplazar_datos_demo_belle.sql reemplaza la información de una base
ya instalada cuando el usuario lo ejecute; cambiar los archivos del proyecto
no borra por sí solo los registros de PostgreSQL.