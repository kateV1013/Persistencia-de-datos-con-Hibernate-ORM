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
        String signo = limpio.startsWith("-") ? "-" : "";
        String numero = limpio.substring(signo.length());
        String normal = normalizarSeparadores(numero);
        if (normal == null) {
            // Separadores fuera de lugar, como "12..5" o "1,50,0": no se adivina qué quiso escribir.
            throw new ReglaNegocioException("El " + campo + " tiene separadores mal colocados; escríbelo como 1,500.00");
        }
        limpio = signo + normal;
        try {
            return new BigDecimal(limpio).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            throw new ReglaNegocioException("El " + campo + " debe ser un número, por ejemplo 1,500.00");
        }
    }

    /**
     * Deja el número con punto decimal y sin separadores de miles, o null si están mal puestos.
     * <ul>
     *   <li>"1,500.50" o "1.500,50": el último separador es el decimal; el otro, de miles.</li>
     *   <li>"1,500" o "12,500,000": comas de miles (Nicaragua); "1500,5": coma decimal.</li>
     *   <li>"0.5": punto decimal; "1.500.000": puntos de miles.</li>
     * </ul>
     * Los miles deben ir en grupos de tres dígitos.
     */
    private static String normalizarSeparadores(String numero) {
        int ultimaComa = numero.lastIndexOf(','), ultimoPunto = numero.lastIndexOf('.');
        if (ultimaComa >= 0 && ultimoPunto >= 0) {
            char decimal = ultimaComa > ultimoPunto ? ',' : '.';
            char miles = decimal == ',' ? '.' : ',';
            int corte = numero.lastIndexOf(decimal);
            String entero = numero.substring(0, corte), fraccion = numero.substring(corte + 1);
            if (!fraccion.matches("\\d+") || !conMilesValidos(entero, miles)) return null;
            return entero.replace(String.valueOf(miles), "") + "." + fraccion;
        }
        char separador = ultimaComa >= 0 ? ',' : '.';
        int cuantos = (int) numero.chars().filter(c -> c == separador).count();
        if (cuantos == 0) return numero;
        if (conMilesValidos(numero, separador) && (separador == ',' || cuantos > 1)) {
            return numero.replace(String.valueOf(separador), "");
        }
        if (cuantos == 1) {
            String[] partes = numero.split(java.util.regex.Pattern.quote(String.valueOf(separador)), -1);
            if (partes[1].matches("\\d+")) return partes[0] + "." + partes[1];
        }
        return null;
    }

    /** "1,500,000" con grupos de tres; también admite el número sin separadores. */
    private static boolean conMilesValidos(String entero, char miles) {
        String m = java.util.regex.Pattern.quote(String.valueOf(miles));
        return entero.matches("\\d*") || entero.matches("\\d{1,3}(" + m + "\\d{3})+");
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
