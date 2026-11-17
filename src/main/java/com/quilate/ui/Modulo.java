package com.quilate.ui;

/**
 * Modulos de la aplicacion. Cada uno aparece como tarjeta en el menu principal
 * y se abre en su propia ventana. Para agregar un modulo nuevo basta con
 * agregar una constante aqui.
 */
public enum Modulo {

    VENTAS("Ventas", "Registrar y confirmar ventas", "/fxml/ventas.fxml",
            Iconos.VENTAS, "#806059", Seccion.TIENDA),
    APARTADOS("Apartados", "Reservar piezas para clientes", "/fxml/apartados.fxml",
            Iconos.APARTADOS, "#8D7268", Seccion.TIENDA),
    CLIENTES("Clientes", "Datos e historial de compras", "/fxml/clientes.fxml",
            Iconos.CLIENTES, "#79656C", Seccion.TIENDA),
    INVENTARIO("Inventario", "Piezas, existencias y categorías", "/fxml/inventario.fxml",
            Iconos.INVENTARIO, "#78665C", Seccion.TIENDA),

    PEDIDOS("Pedidos personalizados", "Grabados, reparaciones y cupo semanal", "/fxml/pedidos.fxml",
            Iconos.PEDIDOS, "#946F61", Seccion.TALLER),
    SERVICIOS("Tipos de servicio", "Catálogo y precios base del taller", "/fxml/servicios.fxml",
            Iconos.SERVICIOS, "#987B68", Seccion.TALLER),
    REPORTE_PIEZAS("Reportes y análisis", "Ventas, horarios, clientes y rentabilidad", "/fxml/reporte_piezas.fxml",
            Iconos.REPORTE, "#73656D", Seccion.TALLER);

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
    private final String color;
    private final Seccion seccion;

    Modulo(String titulo, String descripcion, String fxml, String icono, String color, Seccion seccion) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fxml = fxml;
        this.icono = icono;
        this.color = color;
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

    /** Color de acento del modulo (hex). */
    public String color() {
        return color;
    }

    public Seccion seccion() {
        return seccion;
    }

    /** Estilo en linea que define el color de acento para el CSS (-q-acento). */
    public String estiloAcento() {
        return "-q-acento: " + color + ";";
    }
}
