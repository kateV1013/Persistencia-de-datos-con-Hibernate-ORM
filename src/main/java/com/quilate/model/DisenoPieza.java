package com.quilate.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

/** Parámetros reproducibles de la vista previa. Todas las columnas son opcionales para pedidos antiguos. */
@Embeddable
public class DisenoPieza {
    public static final java.util.List<String> FORMAS = java.util.List.of(
            "Anillo", "Placa", "Placa ovalada", "Placa circular", "Placa corazón");
    @Column(name="diseno_texto",length=40) private String texto="";
    @Column(name="diseno_forma",length=16) private String forma="Anillo";
    @Column(name="diseno_acabado",length=16) private String acabado="Oro";
    @Column(name="diseno_fuente",length=16) private String fuente="Clásica";
    @Column(name="diseno_medida_mm",precision=5,scale=2) private BigDecimal medida=new BigDecimal("18.00");
    @Column(name="diseno_ancho_mm",precision=5,scale=2) private BigDecimal ancho=new BigDecimal("5.00");
    @Column(name="diseno_letra_mm",precision=4,scale=2) private BigDecimal letra=new BigDecimal("2.00");
    public DisenoPieza() { }
    public DisenoPieza copia() { DisenoPieza d=new DisenoPieza(); d.texto=texto;d.forma=forma;d.acabado=acabado;d.fuente=fuente;d.medida=medida;d.ancho=ancho;d.letra=letra;return d; }
    public String getTexto(){return texto;} public void setTexto(String v){texto=v;}
    public String getForma(){return forma;} public void setForma(String v){forma=v;}
    public String getAcabado(){return acabado;} public void setAcabado(String v){acabado=v;}
    public String getFuente(){return fuente;} public void setFuente(String v){fuente=v;}
    public BigDecimal getMedida(){return medida;} public void setMedida(BigDecimal v){medida=v;}
    public BigDecimal getAncho(){return ancho;} public void setAncho(BigDecimal v){ancho=v;}
    public BigDecimal getLetra(){return letra;} public void setLetra(BigDecimal v){letra=v;}
}
