package pe.edu.utp.bancosangre.dao.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Repository
public class DashboardDao {
    private final JdbcTemplate jdbc;

    public DashboardDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> consultar() {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.putAll(consultar("inventario"));
        datos.putAll(consultar("solicitudes"));
        datos.putAll(consultar("donantes"));
        return datos;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> consultar(String consulta) {
        String sql = switch (consulta) {
            case "inventario" -> """
                WITH existencias AS (
                    SELECT h.*, c.temp_min, c.temp_max, c.ultima_temperatura
                      FROM hemocomponentes h
                      INNER JOIN unidades_sangre u ON u.id = h.unidad_matriz_id
                      LEFT JOIN camaras_refrigeracion c ON c.id = h.camara_id
                )
                SELECT count(*) FILTER (WHERE estado = 'disponible' AND fecha_vencimiento >= current_date) AS disponibles,
                       count(*) FILTER (WHERE estado = 'disponible' AND fecha_vencimiento BETWEEN current_date AND current_date + 3) AS proximas,
                       count(*) FILTER (WHERE estado = 'cuarentena') AS cuarentena,
                       count(*) FILTER (WHERE estado NOT IN ('despachado','incinerado','fraccionado')
                           AND (camara_id IS NULL OR ultima_temperatura IS NULL
                               OR ultima_temperatura NOT BETWEEN temp_min AND temp_max)) AS conservacion_revisar,
                       count(*) FILTER (WHERE estado <> 'incinerado' AND fecha_vencimiento < current_date) AS vencimiento_revisar,
                       coalesce(round(avg(volumen_ml) FILTER (WHERE estado = 'disponible'), 2), 0) AS volumen_promedio_ml
                  FROM existencias
                """;
            case "solicitudes" -> """
                WITH pedidos AS (
                    SELECT s.*, p.dni, EXISTS (
                        SELECT 1 FROM detalle_despachos d WHERE d.solicitud_id = s.id) AS despachada,
                        EXISTS (SELECT 1 FROM detalle_pruebas_cruzadas pc
                            WHERE pc.solicitud_id = s.id AND pc.compatible) AS prueba_compatible
                      FROM solicitudes_transfusionales s INNER JOIN pacientes p ON p.id = s.paciente_id
                )
                SELECT count(*) FILTER (WHERE s.estado IN ('pendiente','compatible')
                           AND NOT s.despachada) AS solicitudes,
                       count(*) FILTER (WHERE s.prioridad IN ('urgente','emergencia') AND NOT s.despachada) AS urgentes_sin_entrega,
                       count(DISTINCT s.paciente_id) AS pacientes_solicitantes,
                       count(*) FILTER (WHERE s.estado = 'incompatible') AS incompatibles,
                       coalesce(sum(CASE WHEN s.prueba_compatible THEN 1 ELSE 0 END), 0) AS con_prueba_compatible,
                       count(*) FILTER (WHERE s.dni IS NULL OR btrim(s.dni) = '') AS documento_pendiente
                  FROM pedidos s
                """;
            case "donantes" -> """
                WITH historial AS (
                    SELECT u.donante_id, count(DISTINCT u.id) AS unidades,
                           bool_or('reactivo' IN (t.vih, t.hepatitis_b, t.hepatitis_c,
                               t.sifilis, t.chagas, t.htlv)) AS tamizaje_reactivo
                      FROM unidades_sangre u
                      LEFT JOIN detalle_tamizaje t ON t.unidad_matriz_id = u.id
                     WHERE u.donante_id IS NOT NULL
                     GROUP BY u.donante_id HAVING count(DISTINCT u.id) > 0
                ), padron AS (
                    SELECT d.*, coalesce(h.unidades, 0) AS unidades,
                           coalesce(h.tamizaje_reactivo, false) AS tamizaje_reactivo
                      FROM donantes d LEFT JOIN historial h ON h.donante_id = d.id
                )
                SELECT count(*) AS donantes,
                       count(*) FILTER (WHERE diferido) AS diferidos,
                       count(*) FILTER (WHERE NOT diferido AND ultima_donacion <= current_date - 90) AS intervalo_cumplido,
                       count(*) FILTER (WHERE ultima_donacion IS NULL) AS sin_donacion_previa,
                       count(*) FILTER (WHERE correo NOT ILIKE '%@example.test' AND correo LIKE '%@%') AS correo_no_demo,
                       count(*) FILTER (WHERE unidades > 0) AS con_unidades_registradas,
                       count(*) FILTER (WHERE tamizaje_reactivo) AS con_tamizaje_reactivo,
                       (SELECT count(*) FROM (
                           SELECT campana FROM donantes WHERE nullif(btrim(campana),'') IS NOT NULL
                           GROUP BY campana HAVING count(*) >= 2
                       ) grupos) AS campanas_recurrentes
                  FROM padron
                """;
            case "pacientes" -> """
                SELECT count(*) AS pacientes,
                 count(*) FILTER (WHERE EXISTS (SELECT 1 FROM solicitudes_transfusionales s WHERE s.paciente_id = p.id)) AS pacientes_con_solicitud,
                 count(*) FILTER (WHERE grupo_abo IS NULL OR factor_rh IS NULL) AS grupo_por_confirmar
                 FROM pacientes p
                """;
            case "tamizajes" -> """
                SELECT count(*) AS tamizajes,
                 count(*) FILTER (WHERE 'reactivo' IN (vih, hepatitis_b, hepatitis_c, sifilis, chagas, htlv)) AS reactivos,
                 count(*) FILTER (WHERE 'indeterminado' IN (vih, hepatitis_b, hepatitis_c, sifilis, chagas, htlv)) AS indeterminados
                 FROM detalle_tamizaje
                """;
            case "camaras" -> """
                SELECT count(*) AS camaras,
                 count(*) FILTER (WHERE ultima_temperatura NOT BETWEEN temp_min AND temp_max) AS fuera_rango,
                 count(*) FILTER (WHERE ultima_lectura_en IS NULL OR ultima_lectura_en < now() - interval '24 hours') AS lectura_pendiente
                 FROM camaras_refrigeracion
                """;
            case "despachos" -> """
                SELECT count(*) AS entregas,
                 count(DISTINCT autorizado_por) AS responsables,
                 count(*) FILTER (WHERE receptor_id IS NULL OR btrim(receptor_id) = '') AS receptor_pendiente
                 FROM detalle_despachos
                """;
            case "campanas" -> """
                SELECT count(*) AS campanas, coalesce(sum(captados), 0) AS captados,
                 coalesce(max(captados), 0) AS mayor_captacion
                 FROM (SELECT campana, count(*) AS captados FROM donantes
                 WHERE campana IS NOT NULL AND btrim(campana) <> '' AND campana NOT ILIKE '%directa%'
                 GROUP BY campana HAVING count(*) > 0) c
                """;
            default -> throw new IllegalArgumentException("Consulta desconocida");
        };
        return jdbc.queryForMap(sql);
    }
}
