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
        return cliente.getId() == null ? dao.crear(cliente) : dao.modificar(cliente);
    }

    public List<Cliente> listar() {
        return dao.listar();
    }

    public void eliminar(Long id) {
        if (id == null) throw new ValidacionException("Seleccione un cliente.");
        dao.eliminar(id);
    }

    private void validar(Cliente c) {
        if (c.getNombre() == null || c.getNombre().isBlank()) {
            throw new ValidacionException("El nombre es obligatorio.");
        }
        c.setNombre(c.getNombre().trim());
        if (c.getNombre().length() > 60) throw new ValidacionException("Nombre: máximo 60 caracteres.");
        if (c.getCorreo() != null && c.getCorreo().isBlank()) c.setCorreo(null);
        if (c.getTelefono() != null && !c.getTelefono().isBlank()
                && !TELEFONO.matcher(c.getTelefono().trim()).matches()) {
            throw new ValidacionException("El teléfono solo admite dígitos, +, - y espacios (8 a 20 caracteres).");
        }
        if (c.getCorreo() != null && !c.getCorreo().isBlank()
                && !CORREO.matcher(c.getCorreo().trim()).matches()) {
            throw new ValidacionException("El correo no tiene un formato válido.");
        }
    }
}
