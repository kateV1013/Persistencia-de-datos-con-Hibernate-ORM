package com.quilate.util;

import com.quilate.exception.ReglaNegocioException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Conversion de montos y fechas entre la interfaz y el modelo. */
public final class Formatos {

    public static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DecimalFormat DINERO =
            new DecimalFormat("C$ #,##0.00", DecimalFormatSymbols.getInstance(Locale.US));

    private Formatos() {
    }

    /** Convierte el texto de un campo de precio. Acepta "1500", "1,500.50" o "1500,50". */
    public static BigDecimal leerDinero(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            throw new ReglaNegocioException("El " + campo + " es obligatorio.");
        }
        String limpio = texto.trim().replace("C$", "").replace(" ", "");
        // Solo dígitos, signo y separadores: rechaza "1e3", "NaN", letras, etc.
        if (!limpio.matches("-?[0-9.,]+") || !limpio.matches(".*[0-9].*")) {
            throw new ReglaNegocioException("El " + campo + " debe ser un número, por ejemplo 1,500.00");
        }
        int ultimaComa = limpio.lastIndexOf(','), ultimoPunto = limpio.lastIndexOf('.');
        if (ultimaComa >= 0 && ultimoPunto >= 0) {
            // "1,500.50" o "1.500,50": el separador que aparece último es el decimal.
            String miles = ultimaComa > ultimoPunto ? "." : ",";
            limpio = limpio.replace(miles, "").replace(',', '.');
        } else if (ultimaComa >= 0) {
            // "1,500" o "12,500,000" son miles (formato de Nicaragua); "1500,5" es decimal.
            limpio = limpio.matches("-?\\d{1,3}(,\\d{3})+") ? limpio.replace(",", "") : limpio.replace(',', '.');
        } else if (limpio.indexOf('.') != ultimoPunto) {
            // Varios puntos ("1.500.000") solo pueden ser separadores de miles.
            limpio = limpio.replace(".", "");
        }
        try {
            return new BigDecimal(limpio).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            throw new ReglaNegocioException("El " + campo + " debe ser un número, por ejemplo 1,500.00");
        }
    }

    public static String dinero(BigDecimal monto) {
        return monto == null ? "" : DINERO.format(monto);
    }

    /** Valor para un campo de texto editable (sin simbolo de moneda). */
    public static String dineroEditable(BigDecimal monto) {
        return monto == null ? "" : monto.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public static String fecha(LocalDate fecha) {
        return fecha == null ? "" : fecha.format(FECHA);
    }
}
