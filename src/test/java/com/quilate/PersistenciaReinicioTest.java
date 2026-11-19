package com.quilate;

import com.quilate.dao.ClienteDAO;
import com.quilate.model.Cliente;
import com.quilate.util.JPAUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PersistenciaReinicioTest {
    @Test
    void generaElEsquemaYRecuperaElClienteTrasCerrarLaFabrica(@TempDir Path carpeta) {
        var conexion = new HashMap<String, Object>(Map.of(
                "jakarta.persistence.jdbc.driver", "org.h2.Driver",
                "jakarta.persistence.jdbc.url", "jdbc:h2:file:" + carpeta.resolve("quilate") + ";MODE=PostgreSQL",
                "jakarta.persistence.jdbc.user", "sa",
                "jakarta.persistence.jdbc.password", "",
                "hibernate.hbm2ddl.auto", "update"));
        try {
            JPAUtil.inicializar(conexion);
            var clientes = new ClienteDAO();
            var cliente = clientes.crear(new Cliente("Cliente de prueba", null, "reinicio@example.com"));
            JPAUtil.cerrar();
            conexion.put("hibernate.hbm2ddl.auto", "validate");
            JPAUtil.inicializar(conexion);
            assertEquals("reinicio@example.com", clientes.buscarPorId(cliente.getId()).getCorreo());
        } finally {
            JPAUtil.cerrar();
        }
    }
}
