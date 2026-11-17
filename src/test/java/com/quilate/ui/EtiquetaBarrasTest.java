package com.quilate.ui;

import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EtiquetaBarrasTest {
    @Test void recuperaElCodigoOriginalDesdeLasBarras() throws Exception {
        for(String codigo:new String[]{"JB-001","7501000000011","A".repeat(50)}) {
            boolean[] barras=EtiquetaBarras.codificar(codigo);
            int w=(barras.length+20)*3,h=80;int[] pixels=new int[w*h];
            java.util.Arrays.fill(pixels,0xffffff);
            for(int y=0;y<h;y++) for(int x=0;x<barras.length;x++) if(barras[x])
                for(int z=0;z<3;z++) pixels[y*w+(x+10)*3+z]=0;
            var imagen=new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(w,h,pixels)));
            var resultado=new MultiFormatReader().decode(imagen);
            assertEquals(codigo,resultado.getText());assertEquals(BarcodeFormat.CODE_128,resultado.getBarcodeFormat());
        }
    }
    @Test void rechazaCodigosQueNoSePuedenEtiquetar() {
        for(String codigo:new String[]{"","á","A".repeat(51),"ABC\n"})
            assertThrows(IllegalArgumentException.class,()->EtiquetaBarras.codificar(codigo));
    }
}
