package com.quilate.api;

import com.quilate.dao.PiezaDAO;
import com.quilate.exception.ServicioExternoException;
import com.quilate.exception.ValidacionException;
import com.quilate.model.Pieza;
import com.quilate.service.FrankfurterCliente;
import com.quilate.service.GoldApiCliente;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping(value = "/api/piezas", produces = MediaType.APPLICATION_JSON_VALUE)
public class PiezaApiController {

    public record PiezaRespuesta(Long id, String codigo, String nombre, String categoria, String material,
                                 Short quilates, BigDecimal pesoGramos, Integer existencias,
                                 BigDecimal precioCordobas, BigDecimal precioDolares) { }

    private final PiezaDAO dao = new PiezaDAO();
    private final FrankfurterCliente frankfurter = new FrankfurterCliente();
    private final GoldApiCliente goldApi = new GoldApiCliente();

    @GetMapping
    public List<PiezaRespuesta> listar() {
        BigDecimal tasa = tasaOpcional();
        return dao.listarTodas().stream().map(p -> respuesta(p, tasa)).toList();
    }

    @GetMapping("/{id}")
    public PiezaRespuesta buscar(@PathVariable Long id) {
        return respuesta(requerir(id), tasaOpcional());
    }

    @GetMapping("/{id}/valor-oro")
    public Map<String, Object> valorOro(@PathVariable Long id) {
        Pieza pieza = requerir(id);
        if (pieza.getQuilates() == null || pieza.getPesoGramos() == null) {
            throw new ValidacionException("La pieza no tiene quilates o peso registrados.");
        }
        BigDecimal tasa = frankfurter.tasaUsdNio();
        BigDecimal gramoUsd = goldApi.oro().gramoUsd(pieza.getQuilates());
        BigDecimal valorUsd = gramoUsd.multiply(pieza.getPesoGramos()).setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("pieza", respuesta(pieza, tasa));
        respuesta.put("gramoUsd", gramoUsd.setScale(2, RoundingMode.HALF_UP));
        respuesta.put("valorOroUsd", valorUsd);
        respuesta.put("valorOroCordobas", valorUsd.multiply(tasa).setScale(2, RoundingMode.HALF_UP));
        return respuesta;
    }

    private Pieza requerir(Long id) {
        return dao.buscarPorId(id).orElseThrow(() -> new NoSuchElementException("No existe la pieza " + id + "."));
    }

    private BigDecimal tasaOpcional() {
        try {
            return frankfurter.tasaUsdNio();
        } catch (ServicioExternoException e) {
            return null;
        }
    }

    private static PiezaRespuesta respuesta(Pieza p, BigDecimal tasa) {
        BigDecimal cordobas = BigDecimal.valueOf(p.getPrecioVenta()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal dolares = tasa == null ? null : cordobas.divide(tasa, 2, RoundingMode.HALF_UP);
        return new PiezaRespuesta(p.getId(), p.getCodigoBarras(), p.getNombre(),
                p.getCategoria() == null ? null : p.getCategoria().getNombre(),
                p.getMaterial(), p.getQuilates(), p.getPesoGramos(), p.getExistencias(), cordobas, dolares);
    }
}
