package com.quilate.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quilate.exception.ServicioExternoException;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class GoldApiCliente {

    public record PrecioMetal(String metal, BigDecimal onzaUsd, BigDecimal gramo24kUsd, Instant fecha) {

        public BigDecimal gramoUsd(int quilates) {
            return gramo24kUsd.multiply(BigDecimal.valueOf(quilates))
                    .divide(BigDecimal.valueOf(24), 4, RoundingMode.HALF_UP);
        }
    }

    private static final String URL = "https://www.goldapi.io/api/";
    private static final Duration CACHE = Duration.ofHours(1);
    private static final Map<String, PrecioMetal> guardados = new HashMap<>();

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper json = new ObjectMapper();

    public PrecioMetal oro() {
        return consultar("XAU");
    }

    public PrecioMetal plata() {
        return consultar("XAG");
    }

    private synchronized PrecioMetal consultar(String metal) {
        PrecioMetal guardado = guardados.get(metal);
        if (guardado != null && guardado.fecha().plus(CACHE).isAfter(Instant.now())) {
            return guardado;
        }
        String clave = clave();
        if (clave == null) {
            throw new ServicioExternoException("Falta la clave de GoldAPI. Define GOLDAPI_KEY o goldapi.key en db.properties.");
        }
        try {
            HttpRequest solicitud = HttpRequest.newBuilder(URI.create(URL + metal + "/USD"))
                    .header("x-access-token", clave)
                    .timeout(Duration.ofSeconds(10))
                    .GET().build();
            HttpResponse<String> respuesta = http.send(solicitud, HttpResponse.BodyHandlers.ofString());
            if (respuesta.statusCode() != 200) {
                throw new ServicioExternoException("GoldAPI respondió " + respuesta.statusCode() + ".");
            }
            JsonNode datos = json.readTree(respuesta.body());
            PrecioMetal precio = new PrecioMetal(metal,
                    datos.get("price").decimalValue(),
                    datos.get("price_gram_24k").decimalValue(),
                    Instant.now());
            guardados.put(metal, precio);
            return precio;
        } catch (ServicioExternoException e) {
            throw e;
        } catch (Exception e) {
            throw new ServicioExternoException("No se pudo consultar el precio en GoldAPI.");
        }
    }

    private static String clave() {
        String valor = System.getenv("GOLDAPI_KEY");
        if (valor != null && !valor.isBlank()) {
            return valor;
        }
        Path archivo = Path.of("db.properties");
        if (!Files.exists(archivo)) {
            return null;
        }
        try (InputStream in = Files.newInputStream(archivo)) {
            Properties props = new Properties();
            props.load(in);
            valor = props.getProperty("goldapi.key");
            return valor == null || valor.isBlank() ? null : valor.trim();
        } catch (Exception e) {
            return null;
        }
    }
}
