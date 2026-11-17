package com.quilate.ui;

import com.google.zxing.oned.Code128Writer;
import com.quilate.model.Pieza;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Window;

/** Etiqueta Code 128 con margen blanco de 10 módulos a cada lado. */
public final class EtiquetaBarras {
    private EtiquetaBarras() { }
    public static boolean[] codificar(String codigo) {
        if(codigo==null || codigo.isBlank() || codigo.length()>50 ||
                codigo.chars().anyMatch(c -> c<32 || c>126))
            throw new IllegalArgumentException("La etiqueta admite de 1 a 50 caracteres: letras sin tildes, números y signos ASCII.");
        return new Code128Writer().encode(codigo);
    }
    public static VBox crear(String codigo,String nombre) {
        boolean[] barras=codificar(codigo);
        double modulo=1;
        Canvas imagen=new Canvas((barras.length+20)*modulo,64);
        var g=imagen.getGraphicsContext2D();g.setFill(Color.WHITE);g.fillRect(0,0,imagen.getWidth(),64);
        g.setFill(Color.BLACK);
        for(int i=0;i<barras.length;i++) if(barras[i]) g.fillRect((i+10)*modulo,0,modulo,64);
        Label marca=new Label("JOYERIA BELLE"),pieza=new Label(nombre),numero=new Label(codigo);
        for(Label l:new Label[]{marca,pieza,numero}) l.setStyle("-fx-text-fill: black; -fx-font-size: 11px;");
        pieza.setWrapText(true);pieza.setMaxWidth(imagen.getWidth());pieza.setAlignment(Pos.CENTER);
        VBox etiqueta=new VBox(5,marca,pieza,imagen,numero);etiqueta.setAlignment(Pos.CENTER);
        etiqueta.setPadding(new Insets(12));etiqueta.setStyle("-fx-background-color: white;");
        return etiqueta;
    }
    public static void abrir(Window owner,Pieza pieza) {
        VBox etiqueta=crear(pieza.getCodigoBarras(),pieza.getNombre());
        Dialog<Void> dialogo=new Dialog<>();dialogo.initOwner(owner);dialogo.setTitle("Código de barras · Joyeria Belle");
        ButtonType imprimir=new ButtonType("Imprimir etiqueta",ButtonBar.ButtonData.OTHER);
        dialogo.getDialogPane().getButtonTypes().addAll(imprimir,ButtonType.CLOSE);
        Label estado=new Label("Configura el papel y las copias en el diálogo de impresión.");estado.setWrapText(true);
        ScrollPane vista=new ScrollPane(etiqueta);vista.setPrefViewportWidth(Math.min(700,etiqueta.prefWidth(-1)));vista.setPrefViewportHeight(170);
        dialogo.getDialogPane().setContent(new VBox(12,vista,estado));
        dialogo.getDialogPane().getStylesheets().add(Ventanas.hojaEstilos());
        dialogo.getDialogPane().lookupButton(imprimir).addEventFilter(javafx.event.ActionEvent.ACTION,e -> {
            e.consume();PrinterJob trabajo=PrinterJob.createPrinterJob();
            if(trabajo==null) {estado.setText("No hay una impresora disponible. Instala o selecciona una impresora del sistema.");return;}
            if(!trabajo.showPrintDialog(owner)) {trabajo.cancelJob();return;}
            VBox salida=crear(pieza.getCodigoBarras(),pieza.getNombre());
            new javafx.scene.Scene(salida);salida.applyCss();salida.autosize();
            var pagina=trabajo.getJobSettings().getPageLayout();
            if(salida.getWidth()>pagina.getPrintableWidth() || salida.getHeight()>pagina.getPrintableHeight()) {
                trabajo.cancelJob();estado.setText("La etiqueta no cabe en ese papel. Selecciona un tamaño mayor o cambia la orientación.");return;
            }
            boolean ok=trabajo.printPage(salida);
            if(ok) ok=trabajo.endJob();else trabajo.cancelJob();
            estado.setText(ok?"Etiqueta enviada a la impresora.":"No se pudo completar la impresión.");
        });
        dialogo.showAndWait();
    }
}
