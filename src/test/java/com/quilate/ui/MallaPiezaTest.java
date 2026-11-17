package com.quilate.ui;

import javafx.scene.shape.TriangleMesh;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class MallaPiezaTest {
    @Test void anilloYPlacasSonVolumenesCerrados() {
        List<TriangleMesh> mallas=new ArrayList<>();mallas.add(LienzoGrabado.anillo(9,10.5,5));
        for(String forma:List.of("Placa","Placa ovalada","Placa circular","Placa corazón"))
            mallas.add(LienzoGrabado.placa(forma,25,25,1.2));
        for(TriangleMesh m:mallas) {
            Map<String,Integer> lados=new HashMap<>();
            for(int i=0;i<m.getFaces().size();i+=6) {
                int[] v={m.getFaces().get(i),m.getFaces().get(i+2),m.getFaces().get(i+4)};
                for(int j=0;j<3;j++) {int a=v[j],b=v[(j+1)%3];
                    assertNotEquals(a,b);assertTrue(a*3<m.getPoints().size());
                    lados.merge(Math.min(a,b)+":"+Math.max(a,b),1,Integer::sum);
                }
            }
            assertTrue(lados.values().stream().allMatch(n->n==2),"Cada arista debe pertenecer a dos caras");
            for(float p:m.getPoints().toArray(null)) assertTrue(Float.isFinite(p));
        }
    }
}
