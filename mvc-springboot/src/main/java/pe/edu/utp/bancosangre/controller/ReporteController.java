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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.utp.bancosangre.security.RequiereVista;
import pe.edu.utp.bancosangre.service.ReporteService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Controlador REST para la generación y descarga de reportes oficiales (PDF y Excel)
 * mediante JasperReports. Los permisos por rol se declaran con {@link RequiereVista}
 * y los aplica el interceptor, por lo que ningún endpoint nuevo queda abierto por olvido.
 */
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private static final Logger log = LoggerFactory.getLogger(ReporteController.class);
    private static final String LOGO = "reports/img/logo-reporte.png";
    private static final int MAX_DIAS = 365;
    private static final Set<String> GRUPOS_ABO = Set.of("TODOS", "A", "B", "AB", "O");
    private static final String MEDIA_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

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

    // ---------------------------------------------------------------- endpoints

    @GetMapping("/complementarios/{tipo}.pdf")
    @RequiereVista("reportes")
    public ResponseEntity<?> reporteComplementario(@PathVariable("tipo") String tipo,
            @RequestParam(defaultValue = "false") boolean descargar, HttpSession session) {
        String plantilla = COMPLEMENTARIOS.get(tipo);
        if (plantilla == null) return ResponseEntity.notFound().build();
        return pdf(plantilla, Map.of(), descargar, session);
    }

    @GetMapping("/excel/{tipo}.xlsx")
    @RequiereVista("reportes")
    public ResponseEntity<?> reporteExcel(@PathVariable("tipo") String tipo, HttpSession session) {
        String plantilla = switch (tipo) {
            case "donantes" -> "rpt_estatico_donantes";
            case "solicitudes" -> "rpt_dinamico_solicitudes";
            case "unidades-vencer" -> "rpt_dinamico_unidades_por_vencer";
            default -> COMPLEMENTARIOS.get(tipo);
        };
        if (plantilla == null) return ResponseEntity.notFound().build();
        byte[] archivo = reporteService.generarExcel(plantilla, parametrosBase(session, Map.of()));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(MEDIA_XLSX))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(plantilla + ".xlsx").build().toString())
                .body(archivo);
    }

    /** Reporte Estático: Listado general de donantes (RPT-01). */
    @GetMapping("/donantes.pdf")
    @RequiereVista("donantes")
    public ResponseEntity<?> reporteDonantes(
            @RequestParam(value = "descargar", defaultValue = "false") boolean descargar,
            HttpSession session) {
        return pdf("rpt_estatico_donantes", Map.of(), descargar, session);
    }

    /**
     * Reporte Dinámico: Solicitudes transfusionales (RPT-02).
     * Parámetros: fechaInicio, fechaFin, prioridad (TODAS|rutina|urgente|emergencia).
     */
    @GetMapping("/solicitudes.pdf")
    @RequiereVista("solicitudes")
    public ResponseEntity<?> reporteSolicitudes(
            @RequestParam(value = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(value = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(value = "prioridad", defaultValue = "TODAS") String prioridad,
            @RequestParam(value = "descargar", defaultValue = "false") boolean descargar,
            HttpSession session) {
        // Fecha inicio por defecto: 1 de enero del año en curso; fecha fin por defecto: hoy
        LocalDate hoy = LocalDate.now();
        LocalDate desde = fechaInicio != null ? fechaInicio : hoy.withDayOfYear(1);
        LocalDate hasta = fechaFin != null ? fechaFin : hoy;
        if (desde.isAfter(hasta)) {
            return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN)
                    .body("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
        Date inicio = aFecha(desde);
        Date fin = fechaFin != null ? aFecha(hasta) : new Date();
        String prioridadFiltrada = (prioridad == null || prioridad.isBlank()) ? "TODAS" : prioridad.trim();
        if (prioridadFiltrada.length() > 30) {
            return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN).body("Prioridad no válida.");
        }

        return pdf("rpt_dinamico_solicitudes", Map.of(
                "FECHA_INICIO", inicio,
                "FECHA_FIN", fin,
                "PRIORIDAD", prioridadFiltrada), descargar, session);
    }

    /**
     * Reporte Dinámico: Unidades de sangre por vencer - FEFO (RPT-03).
     * Parámetros: dias (horizonte en días), grupoAbo (TODOS|A|B|AB|O).
     */
    @GetMapping("/unidades-vencer.pdf")
    @RequiereVista("inventario")
    public ResponseEntity<?> reporteUnidadesPorVencer(
            @RequestParam(value = "dias", defaultValue = "30") Integer dias,
            @RequestParam(value = "grupoAbo", defaultValue = "TODOS") String grupoAbo,
            @RequestParam(value = "descargar", defaultValue = "false") boolean descargar,
            HttpSession session) {
        int horizonte = dias != null ? dias : 30;
        if (horizonte < 1 || horizonte > MAX_DIAS) {
            return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN)
                    .body("El horizonte debe estar entre 1 y " + MAX_DIAS + " días.");
        }
        String grupo = (grupoAbo == null || grupoAbo.isBlank()) ? "TODOS" : grupoAbo.trim().toUpperCase(Locale.ROOT);
        if (!GRUPOS_ABO.contains(grupo)) {
            return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN)
                    .body("Grupo ABO no válido (TODOS, A, B, AB u O).");
        }
        return pdf("rpt_dinamico_unidades_por_vencer", Map.of("DIAS", horizonte, "GRUPO_ABO", grupo), descargar, session);
    }

    // ---------------------------------------------------------------- errores

    /** Registra el detalle en el log y devuelve un mensaje genérico (sin exponer SQL ni rutas internas). */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> alFallar(Exception e) {
        log.error("Error al generar el reporte: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.TEXT_PLAIN)
                .body("No se pudo generar el reporte. Intenta nuevamente o contacta al administrador.");
    }

    // ---------------------------------------------------------------- utilidades

    private ResponseEntity<?> pdf(String plantilla, Map<String, Object> extra, boolean descargar, HttpSession session) {
        byte[] bytes = reporteService.generarPdf(plantilla, parametrosBase(session, extra));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition((descargar ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename(plantilla + ".pdf")
                .build());
        headers.setContentLength(bytes.length);
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    /** Parámetros comunes a todos los reportes (usuario y logo) más los propios de cada uno. */
    private Map<String, Object> parametrosBase(HttpSession session, Map<String, Object> extra) {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("USUARIO", resolverUsuario(session));
        parametros.put("LOGO", LOGO);
        parametros.putAll(extra);
        return parametros;
    }

    private String resolverUsuario(HttpSession session) {
        Object usuario = session == null ? null : session.getAttribute(AuthController.SESSION_USUARIO);
        return usuario != null ? String.valueOf(usuario) : "Sistema";
    }

    private static Date aFecha(LocalDate fecha) {
        return Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
