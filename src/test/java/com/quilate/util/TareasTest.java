package com.quilate.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TareasTest {

    @Test
    void sinBaseDeDatosMuestraUnMensajeClaro() {
        var error = new IllegalStateException("No se pudo abrir la sesión",
                new RuntimeException("Error calling Driver.connect()", new java.net.ConnectException("Connection refused")));
        assertTrue(Tareas.mensajeCorto(error).startsWith("No hay conexión con la base de datos"));
    }

    @Test
    void muestraElMensajeDeLaAppAunqueTraigaUnErrorTecnicoComoCausa() {
        var tecnico = new RuntimeException("ERROR: duplicate key value violates unique constraint \"piezas_codigo_barras_key\"");
        var error = new IllegalArgumentException("Ya existe una pieza con el código de barras: QL-001", tecnico);
        assertEquals("Ya existe una pieza con el código de barras: QL-001", Tareas.mensajeCorto(error));
    }

    @Test
    void losDemasErroresConservanSuMensaje() {
        var error = new RuntimeException("envoltura", new IllegalArgumentException("El código de barras es obligatorio."));
        assertEquals("El código de barras es obligatorio.", Tareas.mensajeCorto(error));
    }
}
