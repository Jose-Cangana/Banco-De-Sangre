package pe.edu.utp.bancosangre.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pe.edu.utp.bancosangre.controller.AuthController;
import pe.edu.utp.bancosangre.controller.ReporteController;
import pe.edu.utp.bancosangre.model.Rol;
import pe.edu.utp.bancosangre.service.ReporteService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Verifica el RBAC de los endpoints de reportes sin necesitar base de datos. */
class PermisosReportesTest {

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        ReporteService servicio = mock(ReporteService.class);
        when(servicio.generarPdf(anyString(), any())).thenReturn(new byte[]{'%', 'P', 'D', 'F'});
        when(servicio.generarExcel(anyString(), any())).thenReturn(new byte[]{1});
        mvc = MockMvcBuilders.standaloneSetup(new ReporteController(servicio))
                .addInterceptors(new PermisosInterceptor())
                .build();
    }

    private static MockHttpSession sesion(Rol rol) {
        MockHttpSession s = new MockHttpSession();
        s.setAttribute(AuthController.SESSION_ROL, rol);
        s.setAttribute(AuthController.SESSION_USUARIO, "prueba@hlev.gob.pe");
        return s;
    }

    @Test
    void sinSesionDevuelve401EnTodosLosReportes() throws Exception {
        for (String ruta : new String[]{"/api/reportes/donantes.pdf", "/api/reportes/solicitudes.pdf",
                "/api/reportes/unidades-vencer.pdf", "/api/reportes/complementarios/normativo.pdf",
                "/api/reportes/excel/donantes.xlsx"}) {
            mvc.perform(get(ruta)).andExpect(status().isUnauthorized());
        }
    }

    @Test
    void medicoSoloVeSolicitudes() throws Exception {
        MockHttpSession s = sesion(Rol.MEDICO_SOLICITANTE);
        mvc.perform(get("/api/reportes/donantes.pdf").session(s)).andExpect(status().isForbidden());
        mvc.perform(get("/api/reportes/unidades-vencer.pdf").session(s)).andExpect(status().isForbidden());
        mvc.perform(get("/api/reportes/complementarios/normativo.pdf").session(s)).andExpect(status().isForbidden());
        mvc.perform(get("/api/reportes/solicitudes.pdf").session(s)).andExpect(status().isOk());
    }

    @Test
    void tecnologoNoAccedeAReportesGerenciales() throws Exception {
        MockHttpSession s = sesion(Rol.TECNOLOGO_MEDICO);
        mvc.perform(get("/api/reportes/donantes.pdf").session(s)).andExpect(status().isOk());
        mvc.perform(get("/api/reportes/complementarios/epidemiologico.pdf").session(s)).andExpect(status().isForbidden());
        mvc.perform(get("/api/reportes/excel/donantes.xlsx").session(s)).andExpect(status().isForbidden());
    }

    @Test
    void jefeAccedeATodoYElPdfSaleBienFormado() throws Exception {
        MockHttpSession s = sesion(Rol.JEFE_BANCO_SANGRE);
        mvc.perform(get("/api/reportes/complementarios/normativo.pdf").session(s))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
        mvc.perform(get("/api/reportes/excel/solicitudes.xlsx").session(s)).andExpect(status().isOk());
    }

    @Test
    void parametrosInvalidosDan400() throws Exception {
        MockHttpSession s = sesion(Rol.JEFE_BANCO_SANGRE);
        mvc.perform(get("/api/reportes/unidades-vencer.pdf?dias=0").session(s)).andExpect(status().isBadRequest());
        mvc.perform(get("/api/reportes/unidades-vencer.pdf?dias=9999").session(s)).andExpect(status().isBadRequest());
        mvc.perform(get("/api/reportes/unidades-vencer.pdf?grupoAbo=X").session(s)).andExpect(status().isBadRequest());
        mvc.perform(get("/api/reportes/unidades-vencer.pdf?dias=30&grupoAbo=ab").session(s)).andExpect(status().isOk());
        mvc.perform(get("/api/reportes/solicitudes.pdf?fechaInicio=2026-05-01&fechaFin=2026-01-01").session(s))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tipoDesconocidoDa404() throws Exception {
        mvc.perform(get("/api/reportes/complementarios/inexistente.pdf").session(sesion(Rol.ADMINISTRADOR)))
                .andExpect(status().isNotFound());
    }
}
