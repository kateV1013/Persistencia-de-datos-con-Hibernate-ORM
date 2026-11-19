package com.quilate.service;

import com.quilate.dao.ClienteDAO;
import com.quilate.exception.ValidacionException;
import com.quilate.model.Cliente;

import java.util.List;
import java.util.regex.Pattern;

public class ClienteService {

    private static final Pattern TELEFONO = Pattern.compile("^[0-9+\\- ]{8,20}$");
    private static final Pattern CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private final ClienteDAO dao = new ClienteDAO();

    public Cliente guardar(Cliente cliente) {
        validar(cliente);
        if (cliente.getCorreo() != null && dao.existeCorreo(cliente.getCorreo(), cliente.getId())) {
            throw new ValidacionException("Ya existe un cliente con el correo " + cliente.getCorreo() + ".");
        }
        return cliente.getId() == null ? dao.crear(cliente) : dao.modificar(cliente);
    }

    public List<Cliente> listar() {
        return dao.listar();
    }

    public void eliminar(Long id) {
        if (id == null) throw new ValidacionException("Selecciona un cliente.");
        dao.eliminar(id);
    }

    private static boolean contieneLetra(String texto) {
        return texto != null && texto.chars().anyMatch(Character::isLetter);
    }

    private void validar(Cliente c) {
        if (c == null) throw new ValidacionException("Ingresa los datos del cliente.");
        if (c.getNombre() == null || c.getNombre().isBlank()) {
            throw new ValidacionException("El nombre es obligatorio.");
        }
        c.setNombre(c.getNombre().trim());
        if (c.getApellido() != null) c.setApellido(c.getApellido().trim());
        if (c.getTelefono() != null) c.setTelefono(c.getTelefono().trim());
        if (c.getCorreo() != null) c.setCorreo(c.getCorreo().trim());
        if (c.getApellido() != null && c.getApellido().length() > 60)
            throw new ValidacionException("Apellido: máximo 60 caracteres.");
        if (c.getCorreo() != null && c.getCorreo().length() > 100)
            throw new ValidacionException("Correo: máximo 100 caracteres.");
        if (c.getNombre().length() > 60) throw new ValidacionException("Nombre: máximo 60 caracteres.");
        if (c.getCorreo() != null && c.getCorreo().isBlank()) c.setCorreo(null);
        if (c.getTelefono() != null && !c.getTelefono().isBlank()
                && (!TELEFONO.matcher(c.getTelefono().trim()).matches()
                    || c.getTelefono().chars().filter(Character::isDigit).count() < 8)) {
            throw new ValidacionException("El teléfono debe tener al menos 8 dígitos; también admite +, - y espacios.");
        }
        if (!contieneLetra(c.getNombre())) {
            throw new ValidacionException("El nombre debe contener letras.");
        }
        if (c.getApellido() != null && !c.getApellido().isBlank() && !contieneLetra(c.getApellido())) {
            throw new ValidacionException("El apellido debe contener letras.");
        }
        if (c.getCorreo() != null && !c.getCorreo().isBlank()
                && !CORREO.matcher(c.getCorreo().trim()).matches()) {
            throw new ValidacionException("El correo no tiene un formato válido.");
        }
    }
}
