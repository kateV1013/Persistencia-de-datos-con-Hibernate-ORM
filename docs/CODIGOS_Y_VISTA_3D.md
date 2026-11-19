# Códigos de barras y visor 3D

En Inventario, selecciona una pieza guardada y pulsa **Ver código / etiqueta**.
La ventana muestra la marca, nombre, barras Code 128 e identificador. Pulsa
**Imprimir etiqueta** y elige impresora, papel y copias. Puedes elegir una impresora
PDF instalada para guardar el resultado. Una etiqueta por página. Si no cabe,
selecciona un papel mayor u orientación horizontal; no se reduce la escala de las
barras automáticamente. No se ha probado una impresora física.

Los códigos admiten de 1 a 50 caracteres ASCII imprimibles (por ejemplo QL-001).
El código debe guardarse en Inventario antes de imprimir. ZXing genera el símbolo,
incluida su verificación; las barras se mantienen negras sobre blanco con márgenes
blancos laterales para facilitar la lectura. El lector debe escribir el código
y enviar Enter en Inventario o Ventas.

En Pedidos y en el editor, las piezas se representan mediante mallas 3D:
anillo hueco y placas rectangulares, ovaladas, circulares o de corazón con grosor.
El acabado cambia el material y el grabado se transforma junto a la pieza.
Arrastra para girar, usa la rueda para acercar/alejar y haz doble clic para
restablecer la vista. La orientación no modifica el diseño guardado.
El texto representa visualmente el grabado; no se genera un archivo de fabricación
ni una cavidad mecanizada. Las medidas finales deben revisarse en taller.

JavaFX requiere aceleración gráfica compatible para el visor. Si no está disponible,
la aplicación muestra la vista 2D y lo indica, conservando la edición del pedido.

Referencias técnicas: [ZXing](https://zxing.github.io/zxing/apidocs/com/google/zxing/oned/Code128Writer.html)
y [JavaFX TriangleMesh](https://openjfx.io/javadoc/21/javafx.graphics/javafx/scene/shape/TriangleMesh.html).
