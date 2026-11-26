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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FrankfurterCliente {

    private record Tasa(BigDecimal valor, Instant consultada) { }

    private static final String URL = "https://api.frankfurter.dev/v2/rate/";
    private static final Duration CACHE = Duration.ofHours(1);
    private static final Map<String, Tasa> guardadas = new ConcurrentHashMap<>();

    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private static final ObjectMapper JSON = new ObjectMapper();

    public BigDecimal tasaUsdNio() {
        return tasa("USD", "NIO");
    }

    public BigDecimal tasa(String de, String a) {
        String par = de + "/" + a;
        return guardadas.compute(par, (monedas, guardada) ->
                guardada != null && guardada.consultada().plus(CACHE).isAfter(Instant.now())
                        ? guardada : solicitarTasa(monedas)).valor();
    }

    private Tasa solicitarTasa(String par) {
        try {
            HttpRequest solicitud = HttpRequest.newBuilder(URI.create(URL + par))
                    .timeout(Duration.ofSeconds(10))
                    .GET().build();
            HttpResponse<String> respuesta = HTTP.send(solicitud, HttpResponse.BodyHandlers.ofString());
            if (respuesta.statusCode() != 200) {
                throw new ServicioExternoException("Frankfurter respondió " + respuesta.statusCode() + ".");
            }
            JsonNode datos = JSON.readTree(respuesta.body());
            BigDecimal valor = datos.get("rate").decimalValue();
            return new Tasa(valor, Instant.now());
        } catch (ServicioExternoException e) {
            throw e;
        } catch (Exception e) {
            throw new ServicioExternoException("No se pudo consultar el tipo de cambio en Frankfurter.");
        }
    }
}
