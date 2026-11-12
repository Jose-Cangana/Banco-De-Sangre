package pe.edu.utp.bancosangre.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RolTest {

    @Test
    void auditoriaYAdministracionSoloParaAdministrador() {
        for (Rol r : Rol.values()) {
            boolean esAdmin = r == Rol.ADMINISTRADOR;
            assertEquals(esAdmin, r.puedeVer("auditoria"), r.name());
            assertEquals(esAdmin, r.puedeVer("administracion"), r.name());
        }
    }

    @Test
    void medicoSoloVeSusTresPantallas() {
        assertTrue(Rol.MEDICO_SOLICITANTE.puedeVer("solicitudes"));
        assertFalse(Rol.MEDICO_SOLICITANTE.puedeVer("inventario"));
        assertFalse(Rol.MEDICO_SOLICITANTE.puedeVer("reportes"));
    }

    @Test
    void etiquetaDesconocidaFalla() {
        assertEquals(Rol.JEFE_BANCO_SANGRE, Rol.desdeEtiqueta("Jefe de Banco de Sangre"));
        assertThrows(IllegalArgumentException.class, () -> Rol.desdeEtiqueta("Invitado"));
    }
}
