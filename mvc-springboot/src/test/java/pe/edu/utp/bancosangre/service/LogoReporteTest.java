package pe.edu.utp.bancosangre.service;

import net.sf.jasperreports.engine.*;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Comprueba que JasperReports encuentra el logo por la ruta de classpath que usan los reportes. */
class LogoReporteTest {

    @Test
    void elLogoExisteEnElClasspath() {
        assertTrue(new ClassPathResource("reports/img/logo-reporte.png").exists());
    }

    @Test
    void jasperResuelveElLogoDesdeElClasspath() throws Exception {
        String jrxml = """
                <jasperReport xmlns="http://jasperreports.sourceforge.net/jasperreports" name="t" pageWidth="200" pageHeight="200" columnWidth="200" leftMargin="0" rightMargin="0" topMargin="0" bottomMargin="0">
                  <parameter name="LOGO" class="java.lang.String"/>
                  <title><band height="60">
                    <image onErrorType="Error"><reportElement x="0" y="0" width="46" height="46"/>
                      <imageExpression><![CDATA[$P{LOGO}]]></imageExpression></image>
                  </band></title>
                </jasperReport>""";
        JasperReport reporte;
        try (InputStream is = new java.io.ByteArrayInputStream(jrxml.getBytes())) {
            reporte = JasperCompileManager.compileReport(is);
        }
        Map<String, Object> p = new HashMap<>();
        p.put("LOGO", "reports/img/logo-reporte.png");
        JasperPrint print = JasperFillManager.fillReport(reporte, p, new JREmptyDataSource(1));
        List<JRPrintElement> elementos = print.getPages().get(0).getElements();
        assertEquals(1, elementos.size());
        assertTrue(elementos.get(0) instanceof JRPrintImage);
    }
}
