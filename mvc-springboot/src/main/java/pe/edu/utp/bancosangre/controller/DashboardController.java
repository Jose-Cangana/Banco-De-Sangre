package pe.edu.utp.bancosangre.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.utp.bancosangre.dao.jdbc.DashboardDao;

import java.util.Map;
import java.util.Set;

@RestController
public class DashboardController {
    private final DashboardDao dashboard;

    public DashboardController(DashboardDao dashboard) {
        this.dashboard = dashboard;
    }

    @GetMapping("/api/dashboard/{consulta}")
    public ResponseEntity<?> consultar(@PathVariable("consulta") String consulta, HttpSession session) {
        if (session.getAttribute(AuthController.SESSION_ROL) == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Sesión vencida. Vuelve a iniciar sesión."));
        }
        if (!Set.of("inventario", "solicitudes", "donantes", "pacientes", "tamizajes", "camaras", "despachos", "campanas").contains(consulta)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dashboard.consultar(consulta));
    }
}
