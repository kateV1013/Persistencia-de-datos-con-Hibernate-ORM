package com.quilate.util;

import com.quilate.exception.ReglaNegocioException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Montos escritos por el usuario en los formularios (córdobas, formato de Nicaragua). */
class FormatosTest {

    @ParameterizedTest(name = "«{0}» = {1}")
    @CsvSource(delimiter = '|', value = {
            "1500         | 1500.00",
            "1,500        | 1500.00",
            "C$ 1,500     | 1500.00",
            "1,500.50     | 1500.50",
            "12,500,000   | 12500000.00",
            "1500,5       | 1500.50",
            "1.500,50     | 1500.50",
            "1.500.000    | 1500000.00",
            "99.999       | 100.00",
            "0.5          | 0.50",
            "-5           | -5.00"})
    void leeMontosEnLosFormatosHabituales(String texto, String esperado) {
        assertEquals(new BigDecimal(esperado), Formatos.leerDinero(texto, "precio"));
    }

    @ParameterizedTest(name = "«{0}» se rechaza")
    @ValueSource(strings = {"abc", "1e3", "NaN", "C$", ",", "12a", "Infinity"})
    void rechazaTextoQueNoEsUnMonto(String texto) {
        assertThrows(ReglaNegocioException.class, () -> Formatos.leerDinero(texto, "precio"));
    }

    @ParameterizedTest(name = "«{0}» se rechaza por separadores mal puestos")
    @ValueSource(strings = {"12..5", "1,50,0", "1.5.0", "1,500.50.3", "1.500,50,3", "5.", "1,,500", "15,00.50"})
    void rechazaSeparadoresMalColocados(String texto) {
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class, () -> Formatos.leerDinero(texto, "precio"));
        assertEquals("El precio tiene separadores mal colocados; escríbelo como 1,500.00", e.getMessage());
    }
}
