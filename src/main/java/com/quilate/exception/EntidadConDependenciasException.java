package com.quilate.exception;

/**
 * Excepción lanzada cuando se intenta eliminar un registro que ya
 * tiene registros dependientes en otra tabla (restricción de FK).
 *
 * <p>Usada en los DAOs para convertir excepciones de BD en un mensaje
 * claro y manejable desde el controlador JavaFX.</p>
 */
public class EntidadConDependenciasException extends RuntimeException {

    public EntidadConDependenciasException(String mensaje) {
        super(mensaje);
    }

    public EntidadConDependenciasException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
