package com.quilate.ui;

import com.quilate.model.DisenoPieza;
import com.quilate.service.DisenoService;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Window;
import java.math.BigDecimal;
import java.util.Optional;

/** Editor modal: cancelar no altera el pedido; aplicar conserva los parámetros hasta Guardar. */
public final class EditorGrabado {
    private EditorGrabado() { }
    public static Optional<DisenoPieza> abrir(Window owner,DisenoPieza previo) {
        DisenoPieza d=previo==null?new DisenoPieza():previo.copia();
        Dialog<DisenoPieza> dialogo=new Dialog<>();dialogo.initOwner(owner);
        dialogo.setTitle("Atelier Joyeria Belle · Diseño de la pieza");
        dialogo.setResizable(true);
        ButtonType aplicar=new ButtonType("Aplicar al pedido",ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(aplicar,ButtonType.CANCEL);
        ((Button)dialogo.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Volver");
        dialogo.getDialogPane().getStylesheets().add(Ventanas.hojaEstilos());
        LienzoGrabado lienzo=new LienzoGrabado();lienzo.setDiseno(d);
        Label titulo=new Label("Una pieza, una historia.");titulo.getStyleClass().add("atelier-title");
        Label subtitulo=new Label("Prueba el texto, el acabado y las proporciones antes de guardar.");subtitulo.getStyleClass().add("ayuda");subtitulo.setWrapText(true);
        Label medidas=new Label();medidas.getStyleClass().add("preview-caption");medidas.setWrapText(true);
        Label nota=new Label("Vista orientativa, sin escala real. El taller debe validar el ajuste y el espacio disponible para grabar.");nota.setWrapText(true);nota.getStyleClass().add("ayuda");
        VBox izquierda=new VBox(14,titulo,subtitulo,lienzo,medidas,nota);izquierda.setPrefWidth(520);VBox.setVgrow(lienzo,Priority.ALWAYS);izquierda.getStyleClass().add("atelier-preview");
        ComboBox<String> forma=combo(DisenoPieza.FORMAS.toArray(String[]::new)),acabado=combo("Oro","Plata","Oro rosa"),fuente=combo("Clásica","Moderna","Cursiva");
        forma.setId("formaGrabado");acabado.setId("acabadoGrabado");fuente.setId("fuenteGrabado");
        forma.setValue(d.getForma());acabado.setValue(d.getAcabado());fuente.setValue(d.getFuente());
        TextField texto=new TextField(d.getTexto());texto.setPromptText("Ej. M & J · 15.11.2026");
        texto.setId("textoGrabado");
        texto.setTextFormatter(new TextFormatter<String>(c -> c.getControlNewText().codePointCount(0,c.getControlNewText().length())<=40?c:null));
        Spinner<Double> medida=new Spinner<>(),ancho=new Spinner<>(),letra=new Spinner<>(1.0,4.0,d.getLetra().doubleValue(),.1);
        medida.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory("Anillo".equals(d.getForma())?12:15,"Anillo".equals(d.getForma())?28:"Placa circular".equals(d.getForma())?35:60,d.getMedida().doubleValue(),.1));
        ancho.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory("Anillo".equals(d.getForma())?2:8,"Anillo".equals(d.getForma())?12:35,d.getAncho().doubleValue(),.1));
        medida.setId("medidaGrabado");ancho.setId("anchoGrabado");letra.setId("letraGrabado");
        for(var sp:java.util.List.of(medida,ancho,letra)){sp.setMaxWidth(Double.MAX_VALUE);sp.setEditable(false);}
        Label nombreMedida=new Label(),nombreAncho=new Label(),error=new Label();error.setWrapText(true);error.getStyleClass().add("mensaje-error");
        Label tituloForm=new Label("Personaliza tu pieza");tituloForm.getStyleClass().add("titulo-seccion");
        VBox controles=new VBox(9,tituloForm,campo("TIPO DE PIEZA",forma),campo("ACABADO VISUAL",acabado),campo("TEXTO DEL GRABADO · MÁX. 40",texto),campo("TIPOGRAFÍA",fuente),nombreMedida,medida,nombreAncho,ancho,campo("ALTURA DE LETRA (MM)",letra),error);
        controles.getStyleClass().add("formulario");controles.setPrefWidth(300);
        Runnable actualizar=() -> {
            boolean circular="Placa circular".equals(forma.getValue());
            ancho.setDisable(circular);
            if(circular && !ancho.getValue().equals(medida.getValue())) ancho.getValueFactory().setValue(medida.getValue());
            d.setForma(forma.getValue());d.setAcabado(acabado.getValue());d.setFuente(fuente.getValue());d.setTexto(texto.getText());
            d.setMedida(BigDecimal.valueOf(medida.getValue()).setScale(2,java.math.RoundingMode.HALF_UP));
            d.setAncho(BigDecimal.valueOf(ancho.getValue()).setScale(2,java.math.RoundingMode.HALF_UP));
            d.setLetra(BigDecimal.valueOf(letra.getValue()).setScale(2,java.math.RoundingMode.HALF_UP));
            boolean anillo="Anillo".equals(d.getForma());nombreMedida.setText(anillo?"DIÁMETRO INTERIOR · 12–28 MM":"ANCHO DE PLACA · 15–60 MM");nombreAncho.setText(anillo?"ANCHO DE BANDA · 2–12 MM":"ALTO DE PLACA · 8–35 MM");
            if(circular) { nombreMedida.setText("DIÁMETRO · 15–35 MM"); nombreAncho.setText("ALTO IGUAL AL DIÁMETRO"); }
            nombreMedida.getStyleClass().setAll("campo-label");nombreAncho.getStyleClass().setAll("campo-label");
            lienzo.setDiseno(d);medidas.setText(d.getForma()+" · "+d.getAcabado()+" · "+d.getMedida()+" × "+d.getAncho()+" mm · letra "+d.getLetra()+" mm");
            try {DisenoService.validar(d);error.setText("");dialogo.getDialogPane().lookupButton(aplicar).setDisable(false);}
            catch(RuntimeException e){error.setText(e.getMessage());dialogo.getDialogPane().lookupButton(aplicar).setDisable(true);}
        };
        forma.valueProperty().addListener((o,a,b)-> {
            boolean anillo="Anillo".equals(b);
            double maximo=anillo?28:"Placa circular".equals(b)?35:60;
            medida.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(anillo?12:15,maximo,Math.max(anillo?12:15,Math.min(maximo,medida.getValue())),.1));
            ancho.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(anillo?2:8,anillo?12:35,Math.max(anillo?2:8,Math.min(anillo?12:35,ancho.getValue())),.1));
        });
        for(var cb:java.util.List.of(forma,acabado,fuente))cb.valueProperty().addListener((o,a,b)->actualizar.run());
        texto.textProperty().addListener((o,a,b)->actualizar.run());
        for(var sp:java.util.List.of(medida,ancho,letra))sp.valueProperty().addListener((o,a,b)->actualizar.run());
        ScrollPane scroll=new ScrollPane(controles);scroll.setFitToWidth(true);scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);scroll.setPrefWidth(320);
        HBox contenido=new HBox(22,izquierda,scroll);contenido.setAlignment(Pos.TOP_LEFT);contenido.setPrefSize(890,580);HBox.setHgrow(izquierda,Priority.ALWAYS);
        dialogo.getDialogPane().setContent(contenido);dialogo.setResultConverter(b -> b==aplicar?d.copia():null);
        FormulariosVisuales.aplicar(contenido);
        actualizar.run();return dialogo.showAndWait();
    }
    private static ComboBox<String> combo(String... valores){ComboBox<String> c=new ComboBox<>();c.getItems().setAll(valores);c.setMaxWidth(Double.MAX_VALUE);return c;}
    private static VBox campo(String nombre,javafx.scene.Node control){Label l=new Label(nombre);l.getStyleClass().add("campo-label");return new VBox(5,l,control);}
}
