package com.quilate.api;

import com.quilate.service.FrankfurterCliente;
import com.quilate.service.GoldApiCliente;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class MercadoController {

    private final FrankfurterCliente frankfurter = new FrankfurterCliente();
    private final GoldApiCliente goldApi = new GoldApiCliente();

    @GetMapping("/cambio")
    public Map<String, Object> cambio() {
        return Map.of("de", "USD", "a", "NIO", "tasa", frankfurter.tasaUsdNio());
    }

    @GetMapping("/oro")
    public Map<String, Object> oro() {
        return precios(goldApi.oro(), List.of(10, 14, 18, 24));
    }

    @GetMapping("/plata")
    public Map<String, Object> plata() {
        return precios(goldApi.plata(), List.of(24));
    }

    private Map<String, Object> precios(GoldApiCliente.PrecioMetal precio, List<Integer> quilates) {
        BigDecimal tasa = frankfurter.tasaUsdNio();
        Map<String, Object> gramo = new LinkedHashMap<>();
        for (int k : quilates) {
            BigDecimal usd = precio.gramoUsd(k);
            gramo.put(k + "k", Map.of(
                    "usd", usd.setScale(2, RoundingMode.HALF_UP),
                    "nio", usd.multiply(tasa).setScale(2, RoundingMode.HALF_UP)));
        }
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("metal", precio.metal());
        respuesta.put("onzaUsd", precio.onzaUsd());
        respuesta.put("tasaUsdNio", tasa);
        respuesta.put("precioPorGramo", gramo);
        respuesta.put("consultado", precio.fecha());
        return respuesta;
    }
}
