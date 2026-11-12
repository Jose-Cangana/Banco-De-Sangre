package pe.edu.utp.bancosangre.security;

import org.junit.jupiter.api.Test;
import pe.edu.utp.bancosangre.model.Rol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PermisosColeccionesTest {

    @Test
    void medicoNoPuedeEscribirUsuariosNiDonantes() {
        assertEquals("usuarios", PermisosColecciones.primeraNoPermitida(Rol.MEDICO_SOLICITANTE, "{\"usuarios\":[]}"));
        assertEquals("intercambios", PermisosColecciones.primeraNoPermitida(Rol.MEDICO_SOLICITANTE, "{\"solicitudes\":[],\"intercambios\":[]}"));
    }

    @Test
    void medicoPuedeGuardarSolicitudesYSuPantallaEnviaInventario() {
        assertNull(PermisosColecciones.primeraNoPermitida(Rol.MEDICO_SOLICITANTE,
                "{\"solicitudes\":[],\"pruebas\":[],\"inventory\":[],\"auditNuevos\":[]}"));
    }

    @Test
    void soloElAdministradorEscribeUsuarios() {
        assertEquals("usuarios", PermisosColecciones.primeraNoPermitida(Rol.JEFE_BANCO_SANGRE, "{\"usuarios\":[]}"));
        assertNull(PermisosColecciones.primeraNoPermitida(Rol.ADMINISTRADOR, "{\"usuarios\":[]}"));
    }

    @Test
    void jsonInvalidoLoRechazaOtraCapa() {
        assertNull(PermisosColecciones.primeraNoPermitida(Rol.MEDICO_SOLICITANTE, "no es json"));
    }
}
