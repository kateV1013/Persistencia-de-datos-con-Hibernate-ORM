package com.quilate.ui;

import javafx.scene.control.ButtonType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DialogosTest {

    @Test void cancelarYNoSeMuestranComoBotonesSecundarios() {
        assertTrue(Dialogos.esSecundario(ButtonType.CANCEL));
        assertTrue(Dialogos.esSecundario(ButtonType.NO));
        assertTrue(Dialogos.esSecundario(ButtonType.CLOSE));
        assertFalse(Dialogos.esSecundario(ButtonType.OK));
        assertFalse(Dialogos.esSecundario(ButtonType.YES));
    }
}
