package com.quilate.ui;

import com.quilate.model.DisenoPieza;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.*;
import javafx.scene.text.*;

/** Ilustración paramétrica local. No pretende sustituir un plano de fabricación. */
public final class LienzoGrabado extends Region {
    private final Canvas canvas=new Canvas();
    private DisenoPieza diseno;
    public LienzoGrabado() { getChildren().add(canvas); setMinSize(220,150); setPrefSize(540,330); }
    public void setDiseno(DisenoPieza d) { diseno=d==null?null:d.copia(); requestLayout(); }
    @Override protected void layoutChildren() { canvas.setWidth(getWidth()); canvas.setHeight(getHeight()); dibujar(); }
    private void dibujar() {
        GraphicsContext g=canvas.getGraphicsContext2D();
        double w=getWidth(),h=getHeight(),s=Math.min(w/540,h/330);
        g.setFill(Color.web("#F4F1E8"));g.fillRoundRect(0,0,w,h,22,22);
        g.save();g.translate(w/2,h/2);g.scale(s,s);
        DisenoPieza d=diseno==null?new DisenoPieza():diseno;
        Color oscuro,medio,claro;
        switch(d.getAcabado()) {
            case "Plata" -> { oscuro=Color.web("#707D83");medio=Color.web("#BAC5C7");claro=Color.web("#F4F8F7"); }
            case "Oro rosa" -> { oscuro=Color.web("#8F5447");medio=Color.web("#CF9585");claro=Color.web("#F9D7C4"); }
            default -> { oscuro=Color.web("#8A6029");medio=Color.web("#CBA35B");claro=Color.web("#F7E4A7"); }
        }
        Paint metal=new LinearGradient(0,0,1,.8,true,CycleMethod.NO_CYCLE,new Stop(0,oscuro),new Stop(.22,claro),new Stop(.48,medio),new Stop(.73,claro),new Stop(1,oscuro));
        double medida=d.getMedida().doubleValue(),banda=d.getAncho().doubleValue();
        g.setFill(Color.web("#1A352A",.11));g.fillOval(-190,65,380,40);
        String texto=diseno==null?"TU GRABADO":d.getTexto();
        Font font=Font.font("Moderna".equals(d.getFuente())?"Open Sans":"Serif",FontWeight.NORMAL,"Cursiva".equals(d.getFuente())?FontPosture.ITALIC:FontPosture.REGULAR,d.getLetra().doubleValue()*8);
        g.setFont(font);g.setTextAlign(TextAlignment.CENTER);g.setTextBaseline(javafx.geometry.VPos.CENTER);
        double anchoVisible;
        if("Placa".equals(d.getForma())) {
            double a=170+medida*2,alto=70+banda*3;
            anchoVisible=a;
            g.setFill(oscuro);g.fillRoundRect(-a/2,-alto/2-8,a,alto,18,18);
            g.setFill(metal);g.fillRoundRect(-a/2,-alto/2-14,a,alto,18,18);
            g.setStroke(claro);g.setLineWidth(1.4);g.strokeRoundRect(-a/2+6,-alto/2-8,a-12,alto-12,14,14);
            g.setFill(Color.web("#6A5840"));g.fillOval(-a/2+14,-7,9,9);
            g.setFill(oscuro.darker());g.fillText(texto,6,-12,a-55);
        } else {
            double rx=125+medida*2,ry=rx*.39,espesor=8+banda*2;
            anchoVisible=(rx-espesor)*2;
            g.setFill(oscuro);g.fillOval(-rx,-ry-20+espesor,rx*2,ry*2);
            g.setFill(metal);g.fillOval(-rx,-ry-20,rx*2,ry*2);
            g.setStroke(claro);g.setLineWidth(1.2);g.strokeOval(-rx+2,-ry-18,rx*2-4,ry*2-4);
            double interior=rx-espesor*1.3;
            g.setFill(new LinearGradient(0,0,0,1,true,CycleMethod.NO_CYCLE,new Stop(0,oscuro.darker()),new Stop(.6,medio),new Stop(1,oscuro)));
            g.fillOval(-interior,-ry-11,interior*2,ry*1.35);
            g.setFill(Color.web("#F4F1E8"));g.fillOval(-interior+3,-ry+2,interior*2-6,ry*.95);
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
        g.setStroke(Color.web("#819285"));g.setLineWidth(1);g.setLineDashes(3,4);
        g.strokeLine(-anchoVisible/2,y,anchoVisible/2,y);g.setLineDashes();
        g.strokeLine(-anchoVisible/2,y-6,-anchoVisible/2,y+6);g.strokeLine(anchoVisible/2,y-6,anchoVisible/2,y+6);
        g.setFill(Color.web("#536D5F"));g.setFont(Font.font("Open Sans",11));
        g.fillText(("Anillo".equals(d.getForma())?"Diámetro interior: ":"Ancho de placa: ")+d.getMedida().stripTrailingZeros().toPlainString()+" mm",0,133);
        g.restore();
    }
}
