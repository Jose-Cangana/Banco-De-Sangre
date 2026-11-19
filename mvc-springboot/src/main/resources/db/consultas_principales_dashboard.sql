-- Las tres consultas multitabla ejecutadas por DashboardDao.
-- Solo lectura; ejecutar en localhost, base bancosangre.
-- WITH, INNER/LEFT JOIN, EXISTS, FILTER, DISTINCT, CASE, COALESCE,
-- COUNT/SUM/AVG/ROUND/BOOL_OR, GROUP BY/HAVING, AND/OR/NOT,
-- =/<>/</<=/>/>=, IN/NOT IN, BETWEEN/NOT BETWEEN, IS NULL/IS NOT NULL,
-- LIKE/ILIKE/NOT ILIKE, NULLIF y aritmética de fechas.
-- Intervalo cumplido no equivale a aptitud clínica.

-- inventario
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
  FROM existencias;

-- solicitudes
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
  FROM pedidos s;

-- donantes
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
  FROM padron;
