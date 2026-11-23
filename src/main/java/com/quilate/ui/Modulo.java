package com.quilate.ui;

/**
 * Modulos de la aplicacion. Cada uno aparece como fila en el menu principal
 * y se abre en su propia ventana. Para agregar un modulo nuevo basta con
 * agregar una constante aqui.
 */
public enum Modulo {

    VENTAS("Ventas", "Registrar y confirmar ventas", "/fxml/ventas.fxml",
            Iconos.VENTAS, Seccion.TIENDA),
    APARTADOS("Apartados", "Reservar piezas para clientes", "/fxml/apartados.fxml",
            Iconos.APARTADOS, Seccion.TIENDA),
    CLIENTES("Clientes", "Datos e historial de compras", "/fxml/clientes.fxml",
            Iconos.CLIENTES, Seccion.TIENDA),
    INVENTARIO("Inventario", "Piezas, existencias y categorías", "/fxml/inventario.fxml",
            Iconos.INVENTARIO, Seccion.TIENDA),

    PEDIDOS("Pedidos personalizados", "Grabados, reparaciones y cupo semanal", "/fxml/pedidos.fxml",
            Iconos.PEDIDOS, Seccion.TALLER),
    SERVICIOS("Tipos de servicio", "Catálogo y precios base del taller", "/fxml/servicios.fxml",
            Iconos.SERVICIOS, Seccion.TALLER),
    REPORTE_PIEZAS("Reportes y análisis", "Ventas, horarios, clientes y rentabilidad", "/fxml/reporte_piezas.fxml",
            Iconos.REPORTE, Seccion.TALLER);

    public enum Seccion {
        TIENDA("Tienda"),
        TALLER("Taller y reportes");

        private final String titulo;

        Seccion(String titulo) {
            this.titulo = titulo;
        }

        public String titulo() {
            return titulo;
        }
    }

    private final String titulo;
    private final String descripcion;
    private final String fxml;
    private final String icono;
    private final Seccion seccion;

    Modulo(String titulo, String descripcion, String fxml, String icono, Seccion seccion) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fxml = fxml;
        this.icono = icono;
        this.seccion = seccion;
    }

    public String titulo() {
        return titulo;
    }

    public String descripcion() {
        return descripcion;
    }

    public String fxml() {
        return fxml;
    }

    public String icono() {
        return icono;
    }

    public Seccion seccion() {
        return seccion;
    }
}
