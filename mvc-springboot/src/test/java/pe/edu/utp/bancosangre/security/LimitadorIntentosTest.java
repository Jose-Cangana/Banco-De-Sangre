package pe.edu.utp.bancosangre.security;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LimitadorIntentosTest {

    @Test
    void bloqueaTrasCincoFallosYSeLiberaConElTiempo() {
        LimitadorIntentos l = new LimitadorIntentos();
        Instant t0 = Instant.parse("2026-01-01T10:00:00Z");
        for (int i = 0; i < LimitadorIntentos.MAX_FALLOS - 1; i++) l.registrarFallo("u|ip", t0);
        assertFalse(l.bloqueado("u|ip", t0));
        l.registrarFallo("u|ip", t0);
        assertTrue(l.bloqueado("u|ip", t0));
        assertFalse(l.bloqueado("otro|ip", t0), "otra clave no se ve afectada");
        assertFalse(l.bloqueado("u|ip", t0.plus(LimitadorIntentos.VENTANA).plusSeconds(1)));
    }

    @Test
    void unLoginCorrectoReiniciaElContador() {
        LimitadorIntentos l = new LimitadorIntentos();
        for (int i = 0; i < 4; i++) l.registrarFallo("u|ip");
        l.limpiar("u|ip");
        l.registrarFallo("u|ip");
        assertFalse(l.bloqueado("u|ip"));
    }
}
