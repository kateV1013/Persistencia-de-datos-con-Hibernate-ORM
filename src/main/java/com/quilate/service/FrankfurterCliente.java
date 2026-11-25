package com.quilate.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quilate.exception.ServicioExternoException;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class FrankfurterCliente {

    private record Tasa(BigDecimal valor, Instant consultada) { }

    private static final String URL = "https://api.frankfurter.dev/v2/rate/";
    private static final Duration CACHE = Duration.ofHours(1);
    private static final Map<String, Tasa> guardadas = new HashMap<>();

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper json = new ObjectMapper();

    public BigDecimal tasaUsdNio() {
        return tasa("USD", "NIO");
    }

    public synchronized BigDecimal tasa(String de, String a) {
        String par = de + "/" + a;
        Tasa guardada = guardadas.get(par);
        if (guardada != null && guardada.consultada().plus(CACHE).isAfter(Instant.now())) {
            return guardada.valor();
        }
        try {
            HttpRequest solicitud = HttpRequest.newBuilder(URI.create(URL + par))
                    .timeout(Duration.ofSeconds(10))
                    .GET().build();
            HttpResponse<String> respuesta = http.send(solicitud, HttpResponse.BodyHandlers.ofString());
            if (respuesta.statusCode() != 200) {
                throw new ServicioExternoException("Frankfurter respondió " + respuesta.statusCode() + ".");
            }
            JsonNode datos = json.readTree(respuesta.body());
            BigDecimal valor = datos.get("rate").decimalValue();
            guardadas.put(par, new Tasa(valor, Instant.now()));
            return valor;
        } catch (ServicioExternoException e) {
            throw e;
        } catch (Exception e) {
            throw new ServicioExternoException("No se pudo consultar el tipo de cambio en Frankfurter.");
        }
    }
}
