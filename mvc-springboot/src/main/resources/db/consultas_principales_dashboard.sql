-- Consultas principales usadas por los botones del Dashboard.
-- Ejecutar en Query Tool de pgAdmin, base bancosangre.
-- WITH organiza consultas intermedias. LEFT JOIN conserva componentes sin cámara.
-- INNER JOIN vincula solicitudes y pacientes. EXISTS detecta despachos sin duplicar solicitudes.
-- FILTER agrega subconjuntos. AND / OR / NOT combinan condiciones.
-- = / <> / < / <= / >=, IN y BETWEEN comparan estados, intervalos y fechas.
-- IS NULL y NULLIF detectan datos pendientes. COALESCE define un resultado para agregados vacíos.
-- LIKE / ILIKE comparan patrones de texto. DISTINCT evita contar pacientes repetidos.
-- AVG y ROUND calculan el volumen medio en ml, que se muestra separado de los conteos.
-- GROUP BY y HAVING identifican campañas con dos o más donantes.
-- Intervalo cumplido solo verifica la fecha y el diferimiento, no certifica aptitud clínica.
-- Estas tres consultas no modifican datos.

-- inventario
WITH existencias AS (
SELECT h.*, c.temp_min, c.temp_max, c.ultima_temperatura
FROM hemocomponentes h LEFT JOIN camaras_refrigeracion c ON c.id = h.camara_id
)
SELECT count(*) FILTER (WHERE estado = 'disponible' AND fecha_vencimiento >= current_date) AS disponibles,
count(*) FILTER (WHERE estado = 'disponible' AND fecha_vencimiento BETWEEN current_date AND current_date + 3) AS proximas,
count(*) FILTER (WHERE estado = 'cuarentena') AS cuarentena,
count(*) FILTER (WHERE estado NOT IN ('despachado','incinerado','fraccionado')
AND (camara_id IS NULL OR ultima_temperatura NOT BETWEEN temp_min AND temp_max)) AS conservacion_revisar,
count(*) FILTER (WHERE estado <> 'incinerado' AND fecha_vencimiento < current_date) AS vencimiento_revisar,
coalesce(round(avg(volumen_ml) FILTER (WHERE estado = 'disponible'), 2), 0) AS volumen_promedio_ml
FROM existencias;

-- solicitudes
WITH pedidos AS (
SELECT s.*, p.dni, EXISTS (
SELECT 1 FROM detalle_despachos d WHERE d.solicitud_id = s.id) AS despachada
FROM solicitudes_transfusionales s INNER JOIN pacientes p ON p.id = s.paciente_id
)
SELECT count(*) FILTER (WHERE s.estado IN ('pendiente','compatible')
AND NOT s.despachada) AS solicitudes,
count(*) FILTER (WHERE s.prioridad IN ('urgente','emergencia') AND NOT s.despachada) AS urgentes_sin_entrega,
count(DISTINCT s.paciente_id) AS pacientes_solicitantes,
count(*) FILTER (WHERE s.estado = 'incompatible') AS incompatibles,
count(*) FILTER (WHERE s.dni IS NULL OR btrim(s.dni) = '') AS documento_pendiente
FROM pedidos s;

-- donantes
SELECT count(*) AS donantes,
count(*) FILTER (WHERE diferido) AS diferidos,
count(*) FILTER (WHERE NOT diferido AND ultima_donacion <= current_date - 90) AS intervalo_cumplido,
count(*) FILTER (WHERE ultima_donacion IS NULL) AS sin_donacion_previa,
count(*) FILTER (WHERE correo NOT ILIKE '%@example.test' AND correo LIKE '%@%') AS correo_no_demo,
(SELECT count(*) FROM (
SELECT campana FROM donantes WHERE nullif(btrim(campana),'') IS NOT NULL
GROUP BY campana HAVING count(*) >= 2
) grupos) AS campanas_recurrentes
FROM donantes;

