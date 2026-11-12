-- Consultas del Dashboard y práctica SQL en pgAdmin
-- Conectar al servidor localhost:5432, base bancosangre, abrir Query Tool.
-- Seleccionar un bloque y ejecutar con F5. Todas estas consultas son de lectura.
-- Los indicadores se calculan con los registros existentes.
-- Reactivos e indeterminados pueden incluir la misma unidad.

-- Dashboard: pacientes
SELECT count(*) AS pacientes,
 count(*) FILTER (WHERE EXISTS (SELECT 1 FROM solicitudes_transfusionales s WHERE s.paciente_id = p.id)) AS pacientes_con_solicitud,
 count(*) FILTER (WHERE grupo_abo IS NULL OR factor_rh IS NULL) AS grupo_por_confirmar
 FROM pacientes p;

-- Dashboard: tamizajes
SELECT count(*) AS tamizajes,
 count(*) FILTER (WHERE 'reactivo' IN (vih, hepatitis_b, hepatitis_c, sifilis, chagas, htlv)) AS reactivos,
 count(*) FILTER (WHERE 'indeterminado' IN (vih, hepatitis_b, hepatitis_c, sifilis, chagas, htlv)) AS indeterminados
 FROM detalle_tamizaje;

-- Dashboard: camaras
SELECT count(*) AS camaras,
 count(*) FILTER (WHERE ultima_temperatura NOT BETWEEN temp_min AND temp_max) AS fuera_rango,
 count(*) FILTER (WHERE ultima_lectura_en IS NULL OR ultima_lectura_en < now() - interval '24 hours') AS lectura_pendiente
 FROM camaras_refrigeracion;

-- Dashboard: despachos
SELECT count(*) AS entregas,
 count(DISTINCT autorizado_por) AS responsables,
 count(*) FILTER (WHERE receptor_id IS NULL OR btrim(receptor_id) = '') AS receptor_pendiente
 FROM detalle_despachos;

-- Dashboard: campanas
SELECT count(*) AS campanas, coalesce(sum(captados), 0) AS captados,
 coalesce(max(captados), 0) AS mayor_captacion
 FROM (SELECT campana, count(*) AS captados FROM donantes
 WHERE campana IS NOT NULL AND btrim(campana) <> '' AND campana NOT ILIKE '%directa%'
 GROUP BY campana HAVING count(*) > 0) c;

-- 6. INNER JOIN, concatenación || y ORDER BY: identifica al donante de cada unidad.
SELECT u.din, d.dni, d.nombres || ' ' || d.apellidos AS donante, u.estado
FROM unidades_sangre u INNER JOIN donantes d ON d.id = u.donante_id
ORDER BY u.fecha_extraccion DESC LIMIT 50;

-- 7. LEFT JOIN e IS NULL: conserva las unidades sin tamizaje.
SELECT u.din, u.estado
FROM unidades_sangre u LEFT JOIN detalle_tamizaje t ON t.unidad_matriz_id = u.id
WHERE t.id IS NULL ORDER BY u.din;

-- 8. AND, OR, NOT, IN, >=, <=, <>: filtra componentes que necesitan revisión.
SELECT din_derivado, estado, fecha_vencimiento
FROM hemocomponentes
WHERE estado NOT IN ('despachado','incinerado','fraccionado')
  AND (fecha_vencimiento <= current_date + 3 OR estado = 'bloqueado')
  AND din_derivado <> ''
ORDER BY fecha_vencimiento;

-- 9. GROUP BY, HAVING, COUNT, AVG, MIN, MAX y SUM: resumen de volumen.
SELECT tipo, count(*) AS cantidad, round(avg(volumen_ml),2) AS promedio_ml,
 min(volumen_ml) AS minimo_ml, max(volumen_ml) AS maximo_ml,
 sum(volumen_ml) AS total_ml
FROM hemocomponentes GROUP BY tipo HAVING count(*) >= 2 ORDER BY cantidad DESC;
-- Los agregados de volumen omiten valores NULL.

-- 10. CASE, COALESCE y aritmética: clasifica vencimientos por fecha.
SELECT din_derivado, coalesce(volumen_ml,0) AS volumen_para_calculo,
 fecha_vencimiento - current_date AS dias_restantes,
 CASE WHEN fecha_vencimiento < current_date THEN 'Vencido'
      WHEN fecha_vencimiento BETWEEN current_date AND current_date + 3 THEN 'Próximo'
      ELSE 'Vigente' END AS vigencia
FROM hemocomponentes ORDER BY dias_restantes;
-- COALESCE usa cero solo para esta salida, sin modificar la base.

-- 11. ILIKE, DISTINCT y NOT LIKE: búsqueda de nombres.
SELECT DISTINCT nombres FROM pacientes
WHERE nombres ILIKE '%maria%' AND nombres NOT LIKE 'Paciente %'
ORDER BY nombres;
-- ILIKE ignora mayúsculas. No elimina acentos. NOT LIKE excluye el prefijo indicado.
-- Para excluir dígitos se usa el operador regex !~.
SELECT DISTINCT nombres FROM pacientes WHERE nombres !~ '[0-9]' ORDER BY nombres LIMIT 50;

-- 12. EXISTS: pacientes con solicitudes urgentes.
SELECT p.dni, p.nombres FROM pacientes p
WHERE EXISTS (SELECT 1 FROM solicitudes_transfusionales s
              WHERE s.paciente_id = p.id AND s.prioridad IN ('urgente','emergencia'));

-- 13. UNION: documentos registrados como pacientes o donantes, sin duplicados.
SELECT dni FROM pacientes WHERE dni IS NOT NULL
UNION
SELECT dni FROM donantes WHERE dni IS NOT NULL;

-- 14. INTERSECT: documentos presentes en ambos grupos.
SELECT dni FROM pacientes WHERE dni IS NOT NULL
INTERSECT
SELECT dni FROM donantes WHERE dni IS NOT NULL;

-- 15. EXCEPT: documentos de donantes que no están en Pacientes.
SELECT dni FROM donantes WHERE dni IS NOT NULL
EXCEPT
SELECT dni FROM pacientes WHERE dni IS NOT NULL;

-- 16. CTE (WITH), ventana RANK y porcentaje: campañas según captación.
WITH captacion AS (
 SELECT campana, count(*) AS total FROM donantes
 WHERE nullif(trim(campana),'') IS NOT NULL
 GROUP BY campana
)
SELECT campana, total, rank() OVER (ORDER BY total DESC) AS puesto,
 round(100.0 * total / nullif(sum(total) OVER (),0),2) AS porcentaje
FROM captacion ORDER BY puesto, campana;

-- 17. ALL y subconsulta: componentes con el volumen máximo conocido.
SELECT din_derivado, volumen_ml FROM hemocomponentes
WHERE volumen_ml IS NOT NULL AND volumen_ml >= ALL
 (SELECT volumen_ml FROM hemocomponentes WHERE volumen_ml IS NOT NULL);

-- 18. FULL OUTER JOIN y filtros por fecha: días con donaciones o despachos.
WITH donaciones AS (
 SELECT fecha_extraccion::date AS dia, count(*) AS total FROM unidades_sangre
 GROUP BY fecha_extraccion::date
), entregas AS (
 SELECT fecha_despacho::date AS dia, count(*) AS total FROM detalle_despachos
 GROUP BY fecha_despacho::date
)
SELECT coalesce(d.dia,e.dia) AS fecha, coalesce(d.total,0) AS donaciones,
 coalesce(e.total,0) AS entregas
FROM donaciones d FULL OUTER JOIN entregas e ON e.dia=d.dia
ORDER BY fecha DESC LIMIT 30;

