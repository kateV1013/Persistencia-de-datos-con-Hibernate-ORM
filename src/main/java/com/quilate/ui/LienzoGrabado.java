package com.quilate.ui;

import com.quilate.model.DisenoPieza;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.scene.transform.Rotate;

/** Visor 3D local con mallas cerradas, iluminación y controles de cámara. */
public final class LienzoGrabado extends Region {
    private final Group modelo=new Group();
    private final Rotate giroX=new Rotate(-20,Rotate.X_AXIS),giroY=new Rotate(-25,Rotate.Y_AXIS);
    private final Label mensaje=new Label();
    private final SubScene escena;
    private final PerspectiveCamera camara=new PerspectiveCamera(true);
    private final VistaPlanaGrabado alternativa;
    private double ultimoX,ultimoY,zoom=1;
    private DisenoPieza diseno;

    public LienzoGrabado() {
        setMinSize(220,150);setPrefSize(540,330);
        if(Platform.isSupported(ConditionalFeature.SCENE3D)) {
            alternativa=null;
            modelo.getTransforms().addAll(giroX,giroY);
            PointLight luz=new PointLight(Color.WHITE);luz.setTranslateX(-150);luz.setTranslateY(-180);luz.setTranslateZ(-250);
            Group mundo=new Group(modelo,new AmbientLight(Color.color(.55,.55,.55)),luz);
            escena=new SubScene(mundo,540,300,true,SceneAntialiasing.BALANCED);
            escena.setFill(Color.web("#F4F1E8"));camara.setNearClip(.1);camara.setFarClip(5000);camara.setFieldOfView(35);
            escena.setCamera(camara);getChildren().add(escena);
            escena.setOnMousePressed(e->{ultimoX=e.getSceneX();ultimoY=e.getSceneY();});
            escena.setOnMouseDragged(e->{
                giroY.setAngle(giroY.getAngle()+(e.getSceneX()-ultimoX)*.6);
                giroX.setAngle(Math.max(-85,Math.min(85,giroX.getAngle()-(e.getSceneY()-ultimoY)*.6)));
                ultimoX=e.getSceneX();ultimoY=e.getSceneY();
            });
            escena.setOnScroll(e->{zoom=Math.max(.55,Math.min(2.5,zoom*Math.exp(e.getDeltaY()*.002)));requestLayout();e.consume();});
            escena.setOnMouseClicked(e->{if(e.getClickCount()==2){giroX.setAngle(-20);giroY.setAngle(-25);zoom=1;requestLayout();}});
        } else {
            escena=null;alternativa=new VistaPlanaGrabado();getChildren().add(alternativa);
        }
        mensaje.setStyle("-fx-text-fill: #536D5F; -fx-font-size: 10px;");mensaje.setWrapText(true);
        mensaje.setMouseTransparent(true);getChildren().add(mensaje);setDiseno(null);
    }
    public void setDiseno(DisenoPieza d) {
        diseno=d==null?null:d.copia();
        setAccessibleText(d==null?"Sin diseño guardado":d.getForma()+" · "+d.getAcabado()+" · "+d.getTexto());
        if(alternativa!=null) {
            alternativa.setDiseno(d);
            mensaje.setText(d==null?"":"Vista 2D: el equipo no dispone de aceleración 3D.");
        } else {
            modelo.getChildren().clear();
            mensaje.setText(d==null?"Sin diseño guardado":"3D · Arrastra para girar · Rueda para zoom · Doble clic para restablecer");
            if(d!=null) construir();
        }
        requestLayout();
    }
    @Override protected void layoutChildren() {
        double h=Math.max(1,getHeight()-32),w=Math.max(1,getWidth());
        if(escena!=null) {
            escena.setWidth(w);escena.setHeight(h);
            camara.setTranslateZ(-500*Math.max(1,h/w)/zoom);
        } else alternativa.resizeRelocate(0,0,w,h);
        mensaje.resizeRelocate(10,diseno==null?h/2:h+4,Math.max(1,w-20),28);
    }
    private void construir() {
        double medida=diseno.getMedida().doubleValue(),ancho=diseno.getAncho().doubleValue();
        Color color=switch(diseno.getAcabado()) {
            case "Plata" -> Color.web("#BFC7CC");
            case "Oro rosa" -> Color.web("#CF9585");
            default -> Color.web("#D0AA60");
        };
        PhongMaterial metal=new PhongMaterial(color);metal.setSpecularColor(Color.web("#FFF4E6"));metal.setSpecularPower(64);
        boolean anillo="Anillo".equals(diseno.getForma());
        TriangleMesh m=anillo?anillo(medida/2,medida/2+1.5,ancho):placa(diseno.getForma(),medida,ancho,1.2);
        MeshView pieza=new MeshView(m);pieza.setCullFace(CullFace.NONE);pieza.setMaterial(metal);
        modelo.getChildren().add(pieza);
        // Texto sobre la superficie frontal, transformado junto a la geometría.
        if(!diseno.getTexto().isBlank()) {
            if(anillo) {
                int[] letras=diseno.getTexto().codePoints().toArray();
                double radio=medida/2+.75;
                double paso=Math.min(.14,2.3/Math.max(1,letras.length));
                for(int i=0;i<letras.length;i++) {
                    double angulo=(i-(letras.length-1)/2.0)*paso;
                    Text texto=texto(new String(Character.toChars(letras[i])),Math.min(1.1,diseno.getLetra().doubleValue()));
                    texto.setTranslateX(radio*Math.sin(angulo));texto.setTranslateY(radio*Math.cos(angulo));
                    texto.setTranslateZ(-ancho/2-.04);texto.setRotate(-Math.toDegrees(angulo));
                    modelo.getChildren().add(texto);
                }
            } else {
                Text texto=texto(diseno.getTexto(),diseno.getLetra().doubleValue());
                double disponible=medida*("Placa corazón".equals(diseno.getForma())?.55:.72);
                double factor=Math.min(1,disponible/Math.max(.1,texto.getLayoutBounds().getWidth()));
                texto.setScaleX(factor);texto.setScaleY(factor);
                texto.setTranslateZ(-.64);modelo.getChildren().add(texto);
            }
        }
        double escala=220/Math.max(anillo?medida+3:medida,ancho);
        modelo.setScaleX(escala);modelo.setScaleY(escala);modelo.setScaleZ(escala);
    }
    private Text texto(String valor,double tamano) {
        Text t=new Text(valor);
        t.setFont(Font.font("Moderna".equals(diseno.getFuente())?"Open Sans":"Serif",
                "Cursiva".equals(diseno.getFuente())?FontPosture.ITALIC:FontPosture.REGULAR,tamano));
        t.setFill(Color.web("#594238"));t.setX(-t.getLayoutBounds().getWidth()/2);t.setY(tamano*.3);
        return t;
    }
    static TriangleMesh anillo(double interior,double exterior,double alto) {
        TriangleMesh m=new TriangleMesh();m.getTexCoords().addAll(0,0);
        int n=128;
        for(int i=0;i<n;i++) {
            double a=2*Math.PI*i/n;
            for(double[] punto:new double[][]{{interior,-alto/2},{exterior,-alto/2},{exterior,alto/2},{interior,alto/2}})
                m.getPoints().addAll((float)(punto[0]*Math.cos(a)),(float)(punto[0]*Math.sin(a)),(float)punto[1]);
        }
        for(int i=0;i<n;i++) for(int j=0;j<4;j++) {
            int a=i*4+j,b=((i+1)%n)*4+j,c=((i+1)%n)*4+(j+1)%4,d=i*4+(j+1)%4;
            cara(m,a,b,c);cara(m,a,c,d);m.getFaceSmoothingGroups().addAll(1<<j,1<<j);
        }
        return m;
    }
    static TriangleMesh placa(String forma,double ancho,double alto,double grosor) {
        TriangleMesh m=new TriangleMesh();m.getTexCoords().addAll(0,0);
        int n=128;
        m.getPoints().addAll(0,0,(float)(-grosor/2),0,0,(float)(grosor/2));
        for(int i=0;i<n;i++) {
            double t=2*Math.PI*i/n,x,y;
            if("Placa corazón".equals(forma)) {
                x=Math.pow(Math.sin(t),3)*ancho/2;
                y=-(13*Math.cos(t)-5*Math.cos(2*t)-2*Math.cos(3*t)-Math.cos(4*t)+2.5)*alto/29;
            } else if("Placa".equals(forma)) {
                x=Math.copySign(Math.pow(Math.abs(Math.cos(t)),.25),Math.cos(t))*ancho/2;
                y=Math.copySign(Math.pow(Math.abs(Math.sin(t)),.25),Math.sin(t))*alto/2;
            } else {x=Math.cos(t)*ancho/2;y=Math.sin(t)*alto/2;}
            m.getPoints().addAll((float)x,(float)y,(float)(-grosor/2),(float)x,(float)y,(float)(grosor/2));
        }
        for(int i=0;i<n;i++) {
            int a=2+i*2,b=2+((i+1)%n)*2;
            cara(m,0,b,a);cara(m,1,a+1,b+1);cara(m,a,b,b+1);cara(m,a,b+1,a+1);
            m.getFaceSmoothingGroups().addAll(1,2,4,4);
        }
        return m;
    }
    private static void cara(TriangleMesh m,int a,int b,int c) {m.getFaces().addAll(a,0,b,0,c,0);}
}
