package com.quilate.ui;

/**
 * Pantalla que vuelve a leer sus datos cuando el usuario regresa a ella desde otro módulo.
 * Por ejemplo: anular una venta y volver a Inventario muestra el stock devuelto sin pulsar Actualizar.
 *
 * <p>La recarga solo cambia listas y tablas: no limpia lo que el usuario esté escribiendo.</p>
 */
public interface Actualizable {

    void actualizarAlVolver();
}
