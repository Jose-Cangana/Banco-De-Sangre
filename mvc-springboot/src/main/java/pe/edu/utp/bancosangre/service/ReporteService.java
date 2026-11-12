package pe.edu.utp.bancosangre.service;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Map;
import java.util.HashMap;
import java.io.ByteArrayOutputStream;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio encargado de compilar, llenar y exportar reportes de JasperReports.
 * Utiliza el DataSource del sistema para la conexión a la base de datos PostgreSQL
 * y mantiene una caché en memoria de las plantillas compiladas (.jrxml -> JasperReport).
 */
@Service
public class ReporteService {

    private static final Logger log = LoggerFactory.getLogger(ReporteService.class);

    private final DataSource dataSource;
    private final Map<String, JasperReport> cacheCompilados = new ConcurrentHashMap<>();

    public ReporteService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Obtiene de la caché o compila la plantilla .jrxml desde classpath:/reports/{nombreReporte}.jrxml.
     */
    public JasperReport obtenerReporte(String nombreReporte) {
        return cacheCompilados.computeIfAbsent(nombreReporte, nombre -> {
            String ruta = "reports/" + nombre + ".jrxml";
            try {
                ClassPathResource resource = new ClassPathResource(ruta);
                if (!resource.exists()) {
                    throw new IllegalArgumentException("No se encontró el archivo de reporte en el classpath: " + ruta);
                }
                try (InputStream is = resource.getInputStream()) {
                    log.info("Compilando plantilla JasperReports: {}", ruta);
                    return JasperCompileManager.compileReport(is);
                }
            } catch (Exception e) {
                log.error("Error al cargar y compilar el reporte {}: {}", nombre, e.getMessage(), e);
                throw new IllegalStateException("Error al compilar la plantilla de reporte: " + nombre, e);
            }
        });
    }

    /**
     * Genera el PDF del reporte especificado inyectándole los parámetros y la conexión del DataSource.
     */
    public byte[] generarPdf(String nombreReporte, Map<String, Object> parametros) {
        JasperReport jasperReport = obtenerReporte(nombreReporte);
        try (Connection connection = dataSource.getConnection()) {
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, connection);
            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (Exception e) {
            log.error("Error al llenar y exportar el reporte {}: {}", nombreReporte, e.getMessage(), e);
            throw new RuntimeException("No se pudo generar el reporte PDF: " + e.getMessage(), e);
        }
    }

    public byte[] generarExcel(String nombreReporte, Map<String, Object> parametros) {
        Map<String, Object> opciones = new HashMap<>(parametros);
        opciones.put(JRParameter.IS_IGNORE_PAGINATION, true);
        try (Connection conexion = dataSource.getConnection();
             ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            JasperPrint documento = JasperFillManager.fillReport(obtenerReporte(nombreReporte), opciones, conexion);
            JRXlsxExporter exportador = new JRXlsxExporter();
            exportador.setExporterInput(new SimpleExporterInput(documento));
            exportador.setExporterOutput(new SimpleOutputStreamExporterOutput(salida));
            SimpleXlsxReportConfiguration formato = new SimpleXlsxReportConfiguration();
            formato.setOnePagePerSheet(false);
            formato.setRemoveEmptySpaceBetweenRows(true);
            formato.setRemoveEmptySpaceBetweenColumns(true);
            formato.setWhitePageBackground(false);
            formato.setDetectCellType(true);
            formato.setSheetNames(new String[]{"Reporte"});
            exportador.setConfiguration(formato);
            exportador.exportReport();
            return salida.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el archivo Excel", e);
        }
    }
}
