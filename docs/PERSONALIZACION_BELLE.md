# Diseños por pedido de Joyeria Belle

Cada pedido guarda su propio diseño. Un mismo cliente puede tener varias piezas
diferentes. Al seleccionar una fila, la vista muestra el grabado, material,
forma y medidas de ese pedido, junto al nombre del cliente y número de pedido.

Si un pedido antiguo no tiene esos datos, se muestra «Sin diseño guardado».
La descripción del trabajo no permite conocer con certeza todas las medidas
ni el diseño: la aplicación no inventa una pieza a partir de ella.

Selecciona el pedido, pulsa **Diseñar pieza**, configura la pieza, pulsa
**Aplicar al pedido** y luego **Guardar**. Cancelar el editor conserva el diseño
anterior. Cambiar de cliente en el formulario retira el diseño en edición para
evitar asignar a otra persona el grabado anterior; se confirma al guardar.

## Formas disponibles

- Anillo.
- Placa: rectangular con esquinas suaves, compatible con los diseños antiguos.
- Placa ovalada.
- Placa circular: diámetro de 15 a 35 mm, alto sincronizado con el diámetro.
- Placa corazón.

La vista respeta la proporción ancho/alto de la placa. El grabado se mantiene
dentro de su contorno. Es una vista orientativa; el taller debe confirmar la
viabilidad del grabado y la altura de letra sobre la pieza física.

Las nuevas formas utilizan la columna existente diseno_forma (16 caracteres).
No requieren una nueva migración ni borrar pedidos; basta tener aplicada
03_diseno_personalizado.sql. El script 04 incluye seis ejemplos de cada forma
de placa, pero reemplaza los datos: no es necesario ejecutarlo para usar el editor.