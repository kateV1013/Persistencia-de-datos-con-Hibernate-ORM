package com.quilate.api;

import com.quilate.model.Cliente;
import com.quilate.service.ClienteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/clientes", produces = MediaType.APPLICATION_JSON_VALUE)
public class ClienteApiController {

    public record ClienteDatos(Long id, String nombre, String apellido, String telefono, String correo) {

        static ClienteDatos de(Cliente c) {
            return new ClienteDatos(c.getId(), c.getNombre(), c.getApellido(), c.getTelefono(), c.getCorreo());
        }
    }

    private final ClienteService service = new ClienteService();

    @GetMapping
    public List<ClienteDatos> listar() {
        return service.listar().stream().map(ClienteDatos::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteDatos crear(@RequestBody ClienteDatos datos) {
        Cliente cliente = new Cliente(datos.nombre(), datos.apellido(), datos.telefono(), datos.correo());
        return ClienteDatos.de(service.guardar(cliente));
    }
}
