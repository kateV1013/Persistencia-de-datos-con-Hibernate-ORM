package com.quilate.ui;

import com.quilate.model.DisenoPieza;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.*;
import javafx.scene.text.*;

/** Ilustración paramétrica local. No pretende sustituir un plano de fabricación. */
public final class VistaPlanaGrabado extends Region {
    private final Canvas canvas=new Canvas();
    private DisenoPieza diseno;
    public VistaPlanaGrabado() { getChildren().add(canvas); setMinSize(220,150); setPrefSize(540,330); }
    public void setDiseno(DisenoPieza d) {
        diseno=d==null?null:d.copia();
        setAccessibleText(d==null ? "Sin diseño guardado" : d.getForma()+" · "+d.getAcabado()+" · "+d.getTexto());
        requestLayout();
    }
    @Override protected void layoutChildren() { canvas.setWidth(getWidth()); canvas.setHeight(getHeight()); dibujar(); }
    private void dibujar() {
        GraphicsContext g=canvas.getGraphicsContext2D();
        double w=getWidth(),h=getHeight(),s=Math.min(w/540,h/330);
        g.setFill(Color.web("#F7F0EA"));g.fillRoundRect(0,0,w,h,22,22);
        if(diseno==null) {
            g.setFill(Color.web("#7B6255"));g.setFont(Font.font("Open Sans",14));
            g.setTextAlign(TextAlignment.CENTER);g.fillText("Sin diseño guardado",w/2,h/2);
            return;
        }
        g.save();g.translate(w/2,h/2);g.scale(s,s);
        DisenoPieza d=diseno;
        Color oscuro,medio,claro;
        switch(d.getAcabado()) {
            case "Plata" -> { oscuro=Color.web("#707D83");medio=Color.web("#BAC5C7");claro=Color.web("#F4F8F7"); }
            case "Oro rosa" -> { oscuro=Color.web("#8F5447");medio=Color.web("#CF9585");claro=Color.web("#F9D7C4"); }
            default -> { oscuro=Color.web("#8A6029");medio=Color.web("#CBA35B");claro=Color.web("#F7E4A7"); }
        }
        Paint metal=new LinearGradient(0,0,1,.8,true,CycleMethod.NO_CYCLE,new Stop(0,oscuro),new Stop(.22,claro),new Stop(.48,medio),new Stop(.73,claro),new Stop(1,oscuro));
        double medida=d.getMedida().doubleValue(),banda=d.getAncho().doubleValue();
        g.setFill(Color.web("#45342C",.11));g.fillOval(-190,65,380,40);
        String texto=d.getTexto();
        Font font=Font.font("Moderna".equals(d.getFuente())?"Open Sans":"Serif",FontWeight.NORMAL,"Cursiva".equals(d.getFuente())?FontPosture.ITALIC:FontPosture.REGULAR,d.getLetra().doubleValue()*8);
        g.setFont(font);g.setTextAlign(TextAlignment.CENTER);g.setTextBaseline(javafx.geometry.VPos.CENTER);
        double anchoVisible;
        if(!"Anillo".equals(d.getForma())) {
            double escala=Math.min(320/medida,180/banda);
            double a=medida*escala,alto=banda*escala;
            anchoVisible=a;
            g.save();g.translate(0,-12);
            g.setFill(oscuro);g.translate(0,5);trazarPlaca(g,d.getForma(),a,alto);g.fill();g.translate(0,-5);
            trazarPlaca(g,d.getForma(),a,alto);g.setFill(metal);g.fill();
            g.setStroke(claro);g.setLineWidth(1.4);g.stroke();
            g.save();trazarPlaca(g,d.getForma(),a,alto);g.clip();
            g.setFill(oscuro.darker());g.fillText(texto,0,0,a*("Placa corazón".equals(d.getForma())?.62:.72));
            g.restore();g.restore();
        } else {
            double rx=125+medida*2,ry=rx*.39,espesor=8+banda*2;
            anchoVisible=(rx-espesor)*2;
            g.setFill(oscuro);g.fillOval(-rx,-ry-20+espesor,rx*2,ry*2);
            g.setFill(metal);g.fillOval(-rx,-ry-20,rx*2,ry*2);
            g.setStroke(claro);g.setLineWidth(1.2);g.strokeOval(-rx+2,-ry-18,rx*2-4,ry*2-4);
            double interior=rx-espesor*1.3;
            g.setFill(new LinearGradient(0,0,0,1,true,CycleMethod.NO_CYCLE,new Stop(0,oscuro.darker()),new Stop(.6,medio),new Stop(1,oscuro)));
            g.fillOval(-interior,-ry-11,interior*2,ry*1.35);
            g.setFill(Color.web("#F7F0EA"));g.fillOval(-interior+3,-ry+2,interior*2-6,ry*.95);
            int[] caracteres=texto.codePoints().toArray();
            double amplitud=Math.min(2.05,caracteres.length*font.getSize()*.65/(rx*.82));
            if(caracteres.length>0) {
                double size=Math.min(font.getSize(),rx*1.65/Math.max(1,caracteres.length)*1.45);
                g.setFont(Font.font(font.getFamily(),"Cursiva".equals(d.getFuente())?FontPosture.ITALIC:FontPosture.REGULAR,size));
                for(int i=0;i<caracteres.length;i++) {
                    double a=caracteres.length==1?0:-amplitud/2+amplitud*i/(caracteres.length-1);
                    double x=rx*.82*Math.sin(a),y=-12+ry*.77*Math.cos(a)+espesor*.3;
                    g.save();g.translate(x,y);g.rotate(Math.toDegrees(Math.atan2(-ry*.77*Math.sin(a),rx*.82*Math.cos(a))));
                    g.setFill(claro);g.fillText(new String(Character.toChars(caracteres[i])),.4,.6);
                    g.setFill(oscuro.darker());g.fillText(new String(Character.toChars(caracteres[i])),0,0);g.restore();
                }
            }
        }
        double y=112;
        g.setStroke(Color.web("#A38B7E"));g.setLineWidth(1);g.setLineDashes(3,4);
        g.strokeLine(-anchoVisible/2,y,anchoVisible/2,y);g.setLineDashes();
        g.strokeLine(-anchoVisible/2,y-6,-anchoVisible/2,y+6);g.strokeLine(anchoVisible/2,y-6,anchoVisible/2,y+6);
        g.setFill(Color.web("#7B6255"));g.setFont(Font.font("Open Sans",11));
        g.fillText(("Anillo".equals(d.getForma())?"Diámetro interior: ":"Ancho de placa: ")+d.getMedida().stripTrailingZeros().toPlainString()+" mm",0,133);
        g.restore();
    }

    private static void trazarPlaca(GraphicsContext g,String forma,double ancho,double alto) {
        g.beginPath();
        if("Placa corazón".equals(forma)) {
            g.moveTo(0,-alto*.28);
            g.bezierCurveTo(-ancho*.28,-alto*.72,-ancho*.72,-alto*.22,-ancho*.36,alto*.12);
            g.bezierCurveTo(-ancho*.22,alto*.27,-ancho*.08,alto*.42,0,alto*.5);
            g.bezierCurveTo(ancho*.08,alto*.42,ancho*.22,alto*.27,ancho*.36,alto*.12);
            g.bezierCurveTo(ancho*.72,-alto*.22,ancho*.28,-alto*.72,0,-alto*.28);
        } else if("Placa ovalada".equals(forma)||"Placa circular".equals(forma)) {
            double x=ancho/2,y=alto/2,k=.55228475;
            g.moveTo(x,0);g.bezierCurveTo(x,k*y,k*x,y,0,y);
            g.bezierCurveTo(-k*x,y,-x,k*y,-x,0);
            g.bezierCurveTo(-x,-k*y,-k*x,-y,0,-y);
            g.bezierCurveTo(k*x,-y,x,-k*y,x,0);
        } else {
            double x=ancho/2,y=alto/2,r=Math.min(12,alto/6);
            g.moveTo(-x+r,-y);g.lineTo(x-r,-y);g.quadraticCurveTo(x,-y,x,-y+r);
            g.lineTo(x,y-r);g.quadraticCurveTo(x,y,x-r,y);
            g.lineTo(-x+r,y);g.quadraticCurveTo(-x,y,-x,y-r);
            g.lineTo(-x,-y+r);g.quadraticCurveTo(-x,-y,-x+r,-y);
        }
        g.closePath();
    }
}
