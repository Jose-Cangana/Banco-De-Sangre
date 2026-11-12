package pe.edu.utp.bancosangre.controller;

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.utp.bancosangre.service.ReporteService;
import pe.edu.utp.bancosangre.model.Rol;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para la generación y descarga de reportes oficiales en formato PDF
 * mediante JasperReports.
 */
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private static final Logger log = LoggerFactory.getLogger(ReporteController.class);
    private static final Map<String, String> COMPLEMENTARIOS = Map.of(
            "operacional-mensual", "rpt_dinamico_operacional_mensual",
            "epidemiologico", "rpt_dinamico_epidemiologico",
            "unidades-perdidas", "rpt_dinamico_unidades_perdidas",
            "hemovigilancia", "rpt_dinamico_hemovigilancia",
            "normativo", "rpt_dinamico_normativo",
            "investigacion", "rpt_dinamico_investigacion"
    );
    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    private String resolverUsuario(HttpSession session) {
        if (session != null && session.getAttribute(AuthController.SESSION_USUARIO) != null) {
            return String.valueOf(session.getAttribute(AuthController.SESSION_USUARIO));
        }
        return "Sistema";
    }

    private ResponseEntity<?> construirRespuestaPdf(byte[] pdfBytes, String nombreArchivo, boolean descargar) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        ContentDisposition disposition = (descargar ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename(nombreArchivo)
                .build();
        headers.setContentDisposition(disposition);
        headers.setContentLength(pdfBytes.length);
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/complementarios/{tipo}.pdf")
    public ResponseEntity<?> reporteComplementario(@PathVariable("tipo") String tipo,
            @RequestParam(defaultValue = "false") boolean descargar, HttpSession session) {
        Rol rol = (Rol) session.getAttribute(AuthController.SESSION_ROL);
        if (rol == null || !rol.puedeVer("reportes")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String plantilla = COMPLEMENTARIOS.get(tipo);
        if (plantilla == null) return ResponseEntity.notFound().build();
        try {
            byte[] pdf = reporteService.generarPdf(plantilla, new HashMap<>(Map.of(
                    "USUARIO", resolverUsuario(session), "LOGO", "img/logo-reporte.png")));
            return construirRespuestaPdf(pdf, plantilla + ".pdf", descargar);
        } catch (Exception e) {
            log.error("Error al generar el reporte {}", plantilla, e);
            return ResponseEntity.internalServerError().body("No se pudo generar el reporte.");
        }
    }

    @GetMapping("/excel/{tipo}.xlsx")
    public ResponseEntity<?> reporteExcel(@PathVariable("tipo") String tipo, HttpSession session) {
        Rol rol = (Rol) session.getAttribute(AuthController.SESSION_ROL);
        if (rol == null || !rol.puedeVer("reportes")) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        String plantilla = switch (tipo) {
            case "donantes" -> "rpt_estatico_donantes";
            case "solicitudes" -> "rpt_dinamico_solicitudes";
            case "unidades-vencer" -> "rpt_dinamico_unidades_por_vencer";
            default -> COMPLEMENTARIOS.get(tipo);
        };
        if (plantilla == null) return ResponseEntity.notFound().build();
        try {
            byte[] archivo = reporteService.generarExcel(plantilla, Map.of("USUARIO", resolverUsuario(session)));
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(plantilla + ".xlsx").build().toString())
                    .body(archivo);
        } catch (Exception e) {
            log.error("Error al generar Excel {}", plantilla, e);
            return ResponseEntity.internalServerError().body("No se pudo generar el archivo Excel.");
        }
    }

    /**
     * Reporte Estático: Listado general de donantes (RPT-01).
     */
    @GetMapping("/donantes.pdf")
    public ResponseEntity<?> reporteDonantes(
            @RequestParam(value = "descargar", defaultValue = "false") boolean descargar,
            HttpSession session) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("USUARIO", resolverUsuario(session));
            parametros.put("LOGO", "img/logo-reporte.png");

            byte[] pdf = reporteService.generarPdf("rpt_estatico_donantes", parametros);
            return construirRespuestaPdf(pdf, "rpt_estatico_donantes.pdf", descargar);
        } catch (Exception e) {
            log.error("Error al generar reporte de donantes: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Error al generar el reporte de donantes: " + e.getMessage());
        }
    }

    /**
     * Reporte Dinámico: Solicitudes transfusionales (RPT-02).
     * Parámetros: fechaInicio, fechaFin, prioridad (TODAS|rutina|urgente|emergencia).
     */
    @GetMapping("/solicitudes.pdf")
    public ResponseEntity<?> reporteSolicitudes(
            @RequestParam(value = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(value = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(value = "prioridad", defaultValue = "TODAS") String prioridad,
            @RequestParam(value = "descargar", defaultValue = "false") boolean descargar,
            HttpSession session) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("USUARIO", resolverUsuario(session));
            parametros.put("LOGO", "img/logo-reporte.png");

            // Fecha inicio por defecto: 2026-01-01
            Date inicio = (fechaInicio != null)
                    ? Date.from(fechaInicio.atStartOfDay(ZoneId.systemDefault()).toInstant())
                    : Date.from(LocalDate.of(2026, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());

            // Fecha fin por defecto: hoy
            Date fin = (fechaFin != null)
                    ? Date.from(fechaFin.atStartOfDay(ZoneId.systemDefault()).toInstant())
                    : new Date();

            parametros.put("FECHA_INICIO", inicio);
            parametros.put("FECHA_FIN", fin);

            String prioridadFiltrada = (prioridad == null || prioridad.isBlank()) ? "TODAS" : prioridad.trim();
            parametros.put("PRIORIDAD", prioridadFiltrada);

            byte[] pdf = reporteService.generarPdf("rpt_dinamico_solicitudes", parametros);
            return construirRespuestaPdf(pdf, "rpt_dinamico_solicitudes.pdf", descargar);
        } catch (Exception e) {
            log.error("Error al generar reporte de solicitudes: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Error al generar el reporte de solicitudes: " + e.getMessage());
        }
    }

    /**
     * Reporte Dinámico: Unidades de sangre por vencer - FEFO (RPT-03).
     * Parámetros: dias (horizonte en días), grupoAbo (TODOS|A|B|AB|O).
     */
    @GetMapping("/unidades-vencer.pdf")
    public ResponseEntity<?> reporteUnidadesPorVencer(
            @RequestParam(value = "dias", defaultValue = "30") Integer dias,
            @RequestParam(value = "grupoAbo", defaultValue = "TODOS") String grupoAbo,
            @RequestParam(value = "descargar", defaultValue = "false") boolean descargar,
            HttpSession session) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("USUARIO", resolverUsuario(session));
            parametros.put("LOGO", "img/logo-reporte.png");
            parametros.put("DIAS", dias != null ? dias : 30);
            parametros.put("GRUPO_ABO", (grupoAbo == null || grupoAbo.isBlank()) ? "TODOS" : grupoAbo.trim());

            byte[] pdf = reporteService.generarPdf("rpt_dinamico_unidades_por_vencer", parametros);
            return construirRespuestaPdf(pdf, "rpt_dinamico_unidades_por_vencer.pdf", descargar);
        } catch (Exception e) {
            log.error("Error al generar reporte de unidades por vencer: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Error al generar el reporte de unidades por vencer: " + e.getMessage());
        }
    }
}
