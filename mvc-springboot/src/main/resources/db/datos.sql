-- =====================================================================
--  Datos de ejemplo iniciales (HLEV - Banco de Sangre Tipo II)
--  Se cargan SOLO si la base está vacía (no hay usuarios), así que es
--  seguro ejecutarlo en cada arranque.
--  Cuentas del equipo del proyecto (7 integrantes). Solo se guarda el HASH bcrypt de cada
--  contraseña, nunca la contraseña en claro. Los DNI de las cuentas son ficticios.
-- =====================================================================
DO $$
DECLARE
    v_tec   uuid;
    v_med   uuid;
    v_hlev  uuid;
BEGIN
    IF EXISTS (SELECT 1 FROM usuarios) THEN
        RETURN;
    END IF;

    -- Usuarios (el más reciente primero, como se listan en Administración) ---
    INSERT INTO usuarios (dni, correo, contrasena_hash, nombres, apellidos, rol, activo, ultimo_acceso, creado_en) VALUES
      ('70000001', 'jcangana@hlev.gob.pe', crypt('Banco2026', gen_salt('bf', 10)), 'José Leonel', 'Cangana Salcedo', 'administrador', true, '2026-09-29 09:12-05', '2026-09-01 10:07-05'),
      ('70000002', 'jpalomino@hlev.gob.pe', crypt('Banco2026', gen_salt('bf', 10)), 'José William', 'Palomino Camarena', 'tecnologo_medico', true, '2026-09-29 08:55-05', '2026-09-01 10:06-05'),
      ('70000003', 'jjaimes@hlev.gob.pe', crypt('Banco2026', gen_salt('bf', 10)), 'Julibeth Antonela', 'Jaimes Daza', 'jefe_banco_sangre', true, '2026-09-28 17:40-05', '2026-09-01 10:05-05'),
      ('70000004', 'dbatallanos@hlev.gob.pe', crypt('Banco2026', gen_salt('bf', 10)), 'Daniel Enrique', 'Batallanos Ibarra', 'medico_solicitante', true, '2026-09-12 11:20-05', '2026-09-01 10:04-05'),
      ('70000005', 'acamones@hlev.gob.pe', crypt('Banco2026', gen_salt('bf', 10)), 'Antony David', 'Camones Chávez', 'tecnologo_medico', true, '2026-09-29 10:20-05', '2026-09-01 10:03-05'),
      ('70000006', 'jyangali@hlev.gob.pe', crypt('Banco2026', gen_salt('bf', 10)), 'Jesús Alberto', 'Yangali Saravia', 'administrador', true, '2026-09-29 07:30-05', '2026-09-01 10:02-05'),
      ('70000007', 'aaylas@hlev.gob.pe', crypt('Banco2026', gen_salt('bf', 10)), 'Alexander Hernan', 'Aylas Valdez', 'medico_solicitante', true, '2026-09-29 07:05-05', '2026-09-01 10:01-05');
    SELECT id INTO v_tec FROM usuarios WHERE correo = 'jpalomino@hlev.gob.pe';
    SELECT id INTO v_med FROM usuarios WHERE correo = 'dbatallanos@hlev.gob.pe';

    -- Establecimientos de la red -------------------------------------------
    INSERT INTO establecimientos_red (nombre, red, convenio_descripcion, convenio_vigente_desde, convenio_vigente_hasta, es_sede_local) VALUES
      ('HLEV (sede local)',              'MINSA - Lima Este', NULL, NULL, NULL, true),
      ('Hospital Vitarte II',            'MINSA - Lima Este', 'Intercambio recíproco de hemocomponentes',            '2026-01-01', '2026-12-31', false),
      ('Hospital de la Solidaridad ATE', 'MINSA - Lima Este', 'Apoyo mutuo ante desabastecimiento crítico',          '2026-06-16', '2027-06-15', false),
      ('PRONAHEBAS Central',             'Red nacional',      'Convenio marco de red nacional de bancos de sangre',  NULL, NULL, false);
    SELECT id INTO v_hlev FROM establecimientos_red WHERE es_sede_local;

    -- Cámaras ---------------------------------------------------------------
    INSERT INTO camaras_refrigeracion (nombre, etiqueta, tipo, temp_min, temp_max, ultima_temperatura, ultima_lectura_en, fuera_de_rango, ultima_calibracion) VALUES
      ('Cámara 1 · Refrigeración', 'Cámara 1 · Refrig.',  'refrigeracion', 2, 6,      4.1,  '2026-09-29 09:00-05', false, '2026-08-15'),
      ('Cámara 2 · Refrigeración', 'Cámara 2 · Refrig.',  'refrigeracion', 2, 6,      6.8,  '2026-09-29 09:00-05', true,  '2026-08-15'),
      ('Congelador 1',             'Congelador 1',        'congelacion',   -30, -18,  -24.3, '2026-09-29 09:00-05', false, '2026-07-20'),
      ('Cámara 3 · Agitador',      'Cámara 3 · Agitador', 'agitador',      20, 24,     NULL, NULL, false, '2026-08-01'),
      ('Congelador 2',             'Congelador 2',        'congelacion',   -30, -18,  NULL, NULL, false, '2026-07-20');

    -- Donantes --------------------------------------------------------------
    INSERT INTO donantes (dni, nombres, apellidos, fecha_nacimiento, sexo, grupo_abo, factor_rh, peso_kg, hemoglobina_gdl,
                          telefono, correo, direccion, antecedentes_viaje, campana, tipo_donacion, total_donaciones,
                          ultima_donacion, apto, diferido, diferimiento_tipo, motivo_diferimiento, diferimiento_hasta,
                          consentimiento_firmado, creado_en) VALUES
      ('45678912', 'María Fernanda', 'Quispe Rojas',   '1994-03-12', 'F', 'O',  'negativo', 62.0, 13.4, '987654321', 'maria.quispe@gmail.com',   'Av. Nicolás Ayllón 1520, Ate',          'Ninguno', 'Dona Vida UTP 2026-II',   'voluntaria', 7,  '2026-09-12', true,  false, NULL,         NULL, NULL, true, '2026-09-01 09:07-05'),
      ('41235678', 'Carlos Alberto', 'Ramírez Soto',   '1988-07-25', 'M', 'A',  'positivo', 78.5, 14.8, '986543210', 'carlos.ramirez@gmail.com', 'Jr. Los Pinos 245, Vitarte',          'Viaje a zona endémica de malaria', 'Donación directa', 'reposicion', 3,  '2026-08-02', false, true,  'Temporal',   'Viaje a zona endémica de malaria', '2026-12-12', true, '2026-09-01 09:06-05'),
      ('72345891', 'Ana Lucía',      'Fernández Luna', '1999-11-03', 'F', 'O',  'positivo', 55.0, 11.2, '985432109', 'ana.fernandez@gmail.com',  'Calle Las Flores 88, Santa Anita',      'Ninguno', 'Empresas Solidarias ATE', 'voluntaria', 12, '2026-08-28', false, true,  'Temporal',   'Hemoglobina bajo el mínimo (11.2 g/dL)', '2026-10-30', true, '2026-09-01 09:05-05'),
      ('09876543', 'Luis Miguel',    'Huamán Castro',  '1985-01-18', 'M', 'B',  'positivo', 84.0, 15.1, '984321098', 'luis.huaman@gmail.com',    'Av. Separadora Industrial 730, Ate',    'Ninguno', 'Donación directa',        'voluntaria', 5,  '2026-06-05', true,  false, NULL,         NULL, NULL, true, '2026-09-01 09:04-05'),
      ('43219876', 'Jorge Antonio',  'Vega Mamani',    '1991-09-09', 'M', 'AB', 'positivo', 72.0, 14.2, '983210987', 'jorge.vega@gmail.com',     'Psje. San Martín 12, Chaclacayo',       'Antecedente de conducta de riesgo declarada', 'Donación directa', 'reposicion', 1,  '2026-06-17', false, true,  'Permanente', 'Antecedente de conducta de riesgo declarada', NULL, true, '2026-09-01 09:03-05'),
      ('70123456', 'Rosa Elena',     'Torres Aliaga',  '1996-05-30', 'F', 'A',  'negativo', 58.5, 13.0, '982109876', 'rosa.torres@gmail.com',    'Av. Carretera Central Km 9, Ate',       'Ninguno', 'Dona Vida UTP 2026-II',   'voluntaria', 9,  '2026-06-10', true,  false, NULL,         NULL, NULL, true, '2026-09-01 09:02-05'),
      ('46801357', 'Pedro Luis',     'Chávez Ibarra',  '1990-12-21', 'M', 'O',  'negativo', 80.0, 15.6, '981098765', 'pedro.chavez@gmail.com',   'Jr. Huáscar 410, Vitarte',              'Ninguno', 'Donación directa',        'autologa',   2,  '2026-09-01', true,  false, NULL,         NULL, NULL, true, '2026-09-01 09:01-05');

    -- Unidades del inventario: (DIN, producto, grupo, cámara, vencimiento, estado, DNI donante, extracción)
    CREATE TEMP TABLE _u (din text, tipo tipo_hemocomponente, abo grupo_abo, rh factor_rh, camara text, vence date,
                          estado estado_unidad, dni text, extraccion timestamptz) ON COMMIT DROP;
    INSERT INTO _u VALUES
      ('W1237250171238', 'concentrado_hematies',    'O',  'negativo', 'Cámara 2 · Refrig.',  '2026-10-11', 'disponible', '45678912', '2026-09-06 09:00-05'),
      ('W1237250171012', 'concentrado_hematies',    'O',  'positivo', 'Cámara 1 · Refrig.',  '2026-09-28', 'reservado',  '72345891', '2026-08-24 09:00-05'),
      ('W1237250169887', 'plasma_fresco_congelado', 'A',  'positivo', 'Congelador 1',        '2027-09-30', 'disponible', '41235678', '2026-08-02 09:00-05'),
      ('W1237250165530', 'concentrado_plaquetas',   'B',  'negativo', 'Cámara 3 · Agitador', '2026-09-16', 'cuarentena', NULL,       '2026-09-11 09:00-05'),
      ('W1237250169901', 'concentrado_hematies',    'AB', 'negativo', 'Cámara 2 · Refrig.',  '2026-09-15', 'bloqueado',  NULL,       '2026-08-11 09:00-05'),
      ('W1237250158820', 'crioprecipitado',         'O',  'positivo', 'Congelador 2',        '2027-04-30', 'disponible', NULL,       '2026-04-30 09:00-05'),
      ('W1237250149310', 'concentrado_hematies',    'A',  'negativo', 'Cámara 1 · Refrig.',  '2026-09-01', 'vencido',    '70123456', '2026-07-28 09:00-05'),
      ('W1237250171400', 'concentrado_hematies',    'B',  'positivo', 'Cámara 2 · Refrig.',  '2026-09-20', 'despachado', '09876543', '2026-08-16 09:00-05'),
      ('W1237260000101', 'sangre_total',            'O',  'negativo', 'Cámara 1 · Refrig.',  '2026-10-06', 'cuarentena', '46801357', '2026-09-01 09:00-05');

    -- Bolsa de sangre con su donante de origen (solo las que tienen donante)
    INSERT INTO unidades_sangre (donante_id, responsable_id, din, grupo_abo, factor_rh, fecha_extraccion, fecha_vencimiento, estado)
    SELECT dn.id, CASE WHEN dn.id IS NOT NULL THEN v_tec END, u.din, u.abo, u.rh, u.extraccion, u.vence, u.estado
      FROM _u u LEFT JOIN donantes dn ON dn.dni = u.dni;

    INSERT INTO hemocomponentes (unidad_matriz_id, codigo_producto, din_derivado, tipo, fecha_vencimiento, estado, camara_id)
    SELECT b.id, tipo_a_codigo(u.tipo), u.din, u.tipo, u.vence, u.estado, c.id
      FROM _u u JOIN unidades_sangre b ON b.din = u.din
      LEFT JOIN camaras_refrigeracion c ON c.etiqueta = u.camara;

    -- Solicitudes de sangre y pruebas de compatibilidad ------------------------
    INSERT INTO pacientes (dni, historia_clinica, nombres, fecha_nacimiento, sexo, grupo_abo, factor_rh,
                           peso_kg, altura_cm, hemoglobina_gdl, telefono, correo, direccion) VALUES
      ('48215637', 'HC-2026-00381', 'Marcos Effio Bravo',     '1979-04-16', 'M', 'A', 'positivo', 74.0, 174, 8.9, '976123450', 'marcos.effio@gmail.com',     'Av. Nicolás Ayllón 2210, Ate'),
      ('41987352', 'HC-2026-00355', 'Rosa Quinteros Farfán',  '1990-08-02', 'F', 'O', 'positivo', 66.5, 162, 9.4, '975234561', 'rosa.quinteros@gmail.com',   'Jr. Huáscar 118, Vitarte'),
      ('73456218', 'HC-2026-00312', 'Denis Ibarra León',      '1984-11-27', 'M', 'B', 'positivo', 81.0, 179, 7.8, '974345672', 'denis.ibarra@gmail.com',     'Calle Los Cedros 45, Santa Anita');

    INSERT INTO solicitudes_transfusionales (codigo, medico_id, servicio, paciente_id, diagnostico_cie10,
           diagnostico_texto, componente, prioridad, prioridad_texto, tipo_solicitud, fecha_liberacion_reserva, reserva_texto, estado, creado_en) VALUES
      ('SOL-2026-0341', v_med, 'Emergencia',        (SELECT id FROM pacientes WHERE historia_clinica = 'HC-2026-00381'), 'K92.2', 'K92.2 — Hemorragia digestiva alta',
       '2 unidades — Concentrado de Hematíes', 'urgente', 'Urgente (<15 min)', 'transfusional', NULL, NULL, 'pendiente',  '2026-09-29 08:40-05'),
      ('SOL-2026-0338', v_med, 'Centro Quirúrgico', (SELECT id FROM pacientes WHERE historia_clinica = 'HC-2026-00355'), 'O72.1', 'O72.1 — Hemorragia posparto',
       '3 unidades — Concentrado de Hematíes', 'rutina',  'Programada',        'reserva',       '2026-09-30', '30/09/2026', 'compatible', '2026-09-28 15:10-05'),
      ('SOL-2026-0333', v_med, 'UCI',               (SELECT id FROM pacientes WHERE historia_clinica = 'HC-2026-00312'), 'D62',   'D62 — Hemorragia aguda, anemia',
       '1 unidad — Plasma Fresco Congelado',   'rutina',  'Diferible',         'transfusional', NULL, NULL, 'pendiente',  '2026-09-27 19:25-05');

    INSERT INTO detalle_pruebas_cruzadas (solicitud_id, hemocomponente_id, prueba_mayor, prueba_menor, rastreo_anticuerpos, compatible,
           validado_por, creado_en, muestra, grupo_paciente, resultado_mayor, resultado_menor, resultado_rai)
    SELECT s.id, h.id, true, true, true, true, v_tec, '2026-09-28 16:30-05', 'S-88213', 'O POS', 'Sin aglutinación', 'Sin aglutinación', 'Negativo'
      FROM solicitudes_transfusionales s, hemocomponentes h
     WHERE s.codigo = 'SOL-2026-0338' AND h.din_derivado = 'W1237250171012';

    -- Hemovigilancia ---------------------------------------------------------
    INSERT INTO eventos_hemovigilancia (hemocomponente_id, donante_id, tipo_evento, reportado_por, creado_en, paciente, din_texto, severidad, estado)
    SELECT h.id, b.donante_id, e.reaccion, v_tec, e.cuando::timestamptz, e.paciente, e.din, e.sev, e.estado
      FROM (VALUES
        ('2026-09-10 10:00-05', 'Torres Huamán, L.', 'Reacción febril no hemolítica',  'W1237250171012', 'amber:Leve',  'green:Resuelto'),
        ('2026-09-03 10:00-05', 'Quiroz Vera, D.',   'Reacción alérgica (urticaria)',  'W1237250169887', 'amber:Leve',  'green:Resuelto'),
        ('2026-08-27 10:00-05', 'Salazar Ponce, R.', 'Sospecha de reacción hemolítica','W1237250165530', 'red:Grave',   'amber:En investigación')
      ) AS e(cuando, paciente, reaccion, din, sev, estado)
      JOIN hemocomponentes h ON h.din_derivado = e.din
      JOIN unidades_sangre b ON b.id = h.unidad_matriz_id;

    -- Intercambios con otros establecimientos ---------------------------------
    INSERT INTO solicitudes_interhospitalarias (establecimiento_origen_id, establecimiento_destino_id, grupo_abo, factor_rh,
           tipo_hemocomponente, cantidad, estado, es_devolucion, creado_en, respondido_en)
    SELECT v_hlev, e.id, 'O'::grupo_abo, 'negativo'::factor_rh, 'concentrado_hematies'::tipo_hemocomponente, 4, 'pendiente'::text, false, '2026-09-29 07:50-05'::timestamptz, NULL::timestamptz
      FROM establecimientos_red e WHERE e.nombre = 'Hospital Vitarte II'
    UNION ALL
    SELECT e.id, v_hlev, 'AB'::grupo_abo, 'positivo'::factor_rh, 'plasma_fresco_congelado'::tipo_hemocomponente, 10, 'aceptada'::text, false, '2026-09-25 11:00-05'::timestamptz, '2026-09-25 15:00-05'::timestamptz
      FROM establecimientos_red e WHERE e.nombre = 'PRONAHEBAS Central';

    -- Alertas y auditoría iniciales --------------------------------------------
    INSERT INTO alertas (tipo, titulo, mensaje, creado_en) VALUES
      ('red',    '3 unidades O− vencen en menos de 24h',      'Priorizar la que vence antes — Cámara 2',       '2026-09-29 09:04-05'),
      ('amber',  'Stock de AB− bajo el mínimo de seguridad',  '9 unidades disponibles, mínimo 20',             '2026-09-29 09:03-05'),
      ('blue',   'Solicitud urgente pendiente — Emergencia',  '2 un. O− · CIE-10 S72.0 · hace 6 min',         '2026-09-29 09:02-05'),
      ('purple', '12 unidades en cuarentena serológica',      'Resultados pendientes del laboratorio',         '2026-09-29 09:01-05');

    INSERT INTO auditoria_logs (usuario_id, accion, entidad, ip_terminal, detalle, creado_en)
    SELECT u.id, a.accion, a.recurso, a.ip, jsonb_build_object('usuario', a.quien), a.cuando::timestamptz
      FROM (VALUES
        ('jjaimes@hlev.gob.pe',  'Jaimes Daza, J. (Jefe BS)',   'Autorizó despacho de unidad',                    'DIN W1237250171238',     '10.20.4.18', '2026-09-15 09:41:02-05'),
        ('jpalomino@hlev.gob.pe','Palomino Camarena, J. (T.M.)','Validó prueba de compatibilidad',                'Solicitud SOL-2026-0341','10.20.4.09', '2026-09-15 09:12:47-05'),
        ('jcangana@hlev.gob.pe', 'Cangana Salcedo, J. (Admin.)',  'Generó la etiqueta de una unidad',               'DIN W1237250171238',     '10.20.4.09', '2026-09-15 08:55:10-05'),
        ('jjaimes@hlev.gob.pe',  'Sistema (automático)',        'Bloqueó unidad por reactividad serológica',      'DIN W1237250169901',     '—',          '2026-09-14 22:03:38-05'),
        ('acamones@hlev.gob.pe', 'Camones Chávez, A. (Backend)','Reordenó el inventario por vencimiento',         'Cámara 2',               '10.20.4.03', '2026-09-14 18:20:15-05')
      ) AS a(correo, quien, accion, recurso, ip, cuando)
      JOIN usuarios u ON u.correo = a.correo;
END $$;

-- La siguiente historia parte del mayor número realmente guardado, sin repetirlo.
SELECT setval(
  'numero_historia_local',
  greatest(coalesce((
    SELECT max(right(historia_clinica, 5)::integer)
      FROM pacientes
     WHERE historia_clinica ~ ('^HC-' || to_char(current_date, 'YYYY') || '-[0-9]{5}$')
  ), 0), 1),
  true
);

-- =====================================================================
-- Datos sintéticos relacionados para pruebas y demostración.
-- Cada tabla recibe 60 filas identificables. Los UUID son deterministas,
-- por lo que este script es idempotente y no duplica datos al reiniciar.
-- =====================================================================
DO $$
DECLARE
    i integer;
    uid uuid;
    did uuid;
    bid uuid;
    cid uuid;
    hid uuid;
    pid uuid;
    sid uuid;
    eid_origen uuid;
    eid_destino uuid;
    abo grupo_abo;
    rh factor_rh;
BEGIN
  -- Primero se crean todos los establecimientos para que las solicitudes
  -- posteriores puedan referenciar origen y destino sin depender del orden.
  FOR i IN 1..60 LOOP
    INSERT INTO establecimientos_red (id,nombre,red,convenio_descripcion,convenio_vigente_desde,convenio_vigente_hasta,es_sede_local)
    VALUES (uuid_generate_v5(uuid_nil(),'banco50:establecimiento:'||i),
      'Establecimiento de prueba '||lpad(i::text,2,'0'),'Red Lima Este','Convenio sintético de intercambio',
      date '2026-01-01',date '2027-12-31',false)
    ON CONFLICT DO NOTHING;
  END LOOP;

  FOR i IN 1..60 LOOP
    uid := uuid_generate_v5(uuid_nil(), 'banco50:usuario:' || i);
    did := uuid_generate_v5(uuid_nil(), 'banco50:donante:' || i);
    bid := uuid_generate_v5(uuid_nil(), 'banco50:unidad:' || i);
    cid := uuid_generate_v5(uuid_nil(), 'banco50:camara:' || i);
    hid := uuid_generate_v5(uuid_nil(), 'banco50:hemocomponente:' || i);
    pid := uuid_generate_v5(uuid_nil(), 'banco50:paciente:' || i);
    sid := uuid_generate_v5(uuid_nil(), 'banco50:solicitud:' || i);
    eid_origen := uuid_generate_v5(uuid_nil(), 'banco50:establecimiento:' || i);
    eid_destino := uuid_generate_v5(uuid_nil(), 'banco50:establecimiento:' || ((i % 60) + 1));
    abo := (ARRAY['O','A','B','AB']::grupo_abo[])[(i % 4) + 1];
    rh := CASE WHEN i % 5 = 0 THEN 'negativo'::factor_rh ELSE 'positivo'::factor_rh END;

    INSERT INTO usuarios (id,dni,correo,contrasena_hash,nombres,apellidos,colegiatura,rol,activo,ultimo_acceso,creado_en)
    VALUES (uid, (71000000+i)::text, 'usuario' || i || '@datos.hlev.gob.pe', crypt('Prueba2026',gen_salt('bf',4)),
            'Usuario ' || lpad(i::text,2,'0'), 'Demostración', 'CMP-' || lpad(i::text,5,'0'),
            (ARRAY['medico_solicitante','tecnologo_medico','jefe_banco_sangre','administrador']::rol_usuario[])[(i % 4)+1],
            true, now()-(i||' hours')::interval, now()-(i||' days')::interval)
    ON CONFLICT DO NOTHING;
    SELECT id INTO uid FROM usuarios WHERE dni = (71000000+i)::text;

    INSERT INTO camaras_refrigeracion (id,nombre,etiqueta,tipo,temp_min,temp_max,ultima_temperatura,ultima_lectura_en,fuera_de_rango,ultima_calibracion)
    VALUES (cid,'Equipo de conservación '||lpad(i::text,2,'0'),'Equipo '||lpad(i::text,2,'0'),
            CASE WHEN i%3=0 THEN 'congelacion' WHEN i%3=1 THEN 'refrigeracion' ELSE 'agitador' END,
            CASE WHEN i%3=0 THEN -30 ELSE 2 END, CASE WHEN i%3=0 THEN -18 ELSE 6 END,
            CASE WHEN i%3=0 THEN -24 ELSE 4 END,now()-(i||' minutes')::interval,false,current_date-(i%120))
    ON CONFLICT DO NOTHING;
    SELECT id INTO cid FROM camaras_refrigeracion WHERE etiqueta = 'Equipo '||lpad(i::text,2,'0');

    INSERT INTO donantes (id,dni,nombres,apellidos,fecha_nacimiento,sexo,grupo_abo,factor_rh,peso_kg,altura_cm,
      hemoglobina_gdl,telefono,correo,direccion,antecedentes_viaje,campana,apto,diferido,consentimiento_firmado,
      creado_en,tipo_donacion,total_donaciones,ultima_donacion,diferimiento_tipo,motivo_diferimiento,diferimiento_hasta)
    VALUES (did,(72000000+i)::text,'Donante '||lpad(i::text,2,'0'),'Sintético Prueba',date '1980-01-01'+(i*83),
      CASE WHEN i%2=0 THEN 'F' ELSE 'M' END,abo,rh,55+(i%35),155+(i%30),12.8+(i%20)/10.0,
      (900000000+i)::text,'donante'||i||'@example.test','Dirección sintética '||i||', Lima','Ninguno','Campaña de prueba',
      i%10<>0,i%10=0,true,now()-(i||' days')::interval,'voluntaria',i%8,current_date-(i%90),
      CASE WHEN i%10=0 THEN 'Temporal' END,CASE WHEN i%10=0 THEN 'Evaluación temporal de demostración' END,
      CASE WHEN i%10=0 THEN current_date+30 END)
    ON CONFLICT DO NOTHING;
    SELECT id INTO did FROM donantes WHERE dni = (72000000+i)::text;

    INSERT INTO pacientes (id,dni,historia_clinica,nombres,fecha_nacimiento,sexo,grupo_abo,factor_rh,peso_kg,altura_cm,
      hemoglobina_gdl,telefono,correo,direccion,creado_en)
    VALUES (pid,(73000000+i)::text,'HC-2026-'||lpad((80000+i)::text,5,'0'),'Paciente '||lpad(i::text,2,'0')||' Sintético',
      date '1975-01-01'+(i*97),CASE WHEN i%2=0 THEN 'F' ELSE 'M' END,abo,rh,50+(i%45),150+(i%40),8+(i%50)/10.0,
      (910000000+i)::text,'paciente'||i||'@example.test','Domicilio sintético '||i||', Lima',now()-(i||' days')::interval)
    ON CONFLICT DO NOTHING;
    SELECT id INTO pid FROM pacientes WHERE dni = (73000000+i)::text;

    INSERT INTO unidades_sangre (id,donante_id,responsable_id,din,din_check_digit,grupo_abo,factor_rh,fecha_extraccion,fecha_vencimiento,estado,creado_en)
    VALUES (bid,did,uid,'WTEST26'||lpad(i::text,6,'0'),lpad((i%97)::text,2,'0'),abo,rh,
      now()-(i||' days')::interval,current_date+35-(i%20),'disponible',now()-(i||' days')::interval)
    ON CONFLICT DO NOTHING;
    SELECT id INTO bid FROM unidades_sangre WHERE din = 'WTEST26'||lpad(i::text,6,'0');

    INSERT INTO hemocomponentes (id,unidad_matriz_id,codigo_producto,din_derivado,tipo,volumen_ml,fecha_vencimiento,estado,camara_id,creado_en)
    VALUES (hid,bid,'E3846V00','WTEST26'||lpad(i::text,6,'0')||'-01','concentrado_hematies',280,
      current_date+35-(i%20),'disponible',cid,now()-(i||' days')::interval)
    ON CONFLICT DO NOTHING;
    SELECT id INTO hid FROM hemocomponentes WHERE din_derivado = 'WTEST26'||lpad(i::text,6,'0')||'-01';

    INSERT INTO detalle_tamizaje (id,unidad_matriz_id,vih,hepatitis_b,hepatitis_c,sifilis,chagas,htlv,doble_digitacion,registrado_por,creado_en)
    VALUES (uuid_generate_v5(uuid_nil(),'banco50:tamizaje:'||i),bid,'no_reactivo','no_reactivo','no_reactivo','no_reactivo','no_reactivo','no_reactivo',true,uid,now()-(i||' days')::interval)
    ON CONFLICT DO NOTHING;

    INSERT INTO solicitudes_transfusionales (id,codigo,medico_id,servicio,paciente_id,diagnostico_cie10,diagnostico_texto,
      componente,prioridad,prioridad_texto,tipo_solicitud,estado,creado_en)
    VALUES (sid,'SOL-DATA-'||lpad(i::text,4,'0'),uid,'Medicina interna',pid,'D64.9','D64.9 — Anemia no especificada',
      '1 unidad — Concentrado de Hematíes','rutina','Diferible','transfusional','compatible',now()-(i||' hours')::interval)
    ON CONFLICT DO NOTHING;
    SELECT id INTO sid FROM solicitudes_transfusionales WHERE codigo = 'SOL-DATA-'||lpad(i::text,4,'0');

    INSERT INTO detalle_pruebas_cruzadas (id,solicitud_id,hemocomponente_id,prueba_mayor,prueba_menor,rastreo_anticuerpos,
      compatible,validado_por,creado_en,muestra,grupo_paciente,resultado_mayor,resultado_menor,resultado_rai)
    VALUES (uuid_generate_v5(uuid_nil(),'banco50:prueba:'||i),sid,hid,true,true,true,true,uid,now()-(i||' hours')::interval,
      'M-'||lpad(i::text,5,'0'),abo::text||CASE WHEN rh='positivo' THEN ' POS' ELSE ' NEG' END,'Sin aglutinación','Sin aglutinación','Negativo')
    ON CONFLICT DO NOTHING;

    INSERT INTO detalle_despachos (id,solicitud_id,hemocomponente_id,escaneo_hemocomponente,escaneo_pulsera_receptor,
      autorizado_por,fecha_despacho,historia_clinica)
    VALUES (uuid_generate_v5(uuid_nil(),'banco50:despacho:'||i),sid,hid,true,true,uid,now()-(i||' hours')::interval,
      'HC-2026-'||lpad((80000+i)::text,5,'0'))
    ON CONFLICT DO NOTHING;

    INSERT INTO eventos_hemovigilancia (id,hemocomponente_id,donante_id,paciente_id,tipo_evento,momento,descripcion,reportado_por,
      creado_en,paciente,din_texto,severidad,estado)
    VALUES (uuid_generate_v5(uuid_nil(),'banco50:hemovigilancia:'||i),hid,did,pid,'Reacción febril no hemolítica','post-transfusional',
      'Evento sintético para validación de trazabilidad',uid,now()-(i||' hours')::interval,'Paciente '||lpad(i::text,2,'0')||' Sintético',
      'WTEST26'||lpad(i::text,6,'0')||'-01','amber:Leve','green:Resuelto')
    ON CONFLICT DO NOTHING;

    INSERT INTO establecimientos_red (id,nombre,red,convenio_descripcion,convenio_vigente_desde,convenio_vigente_hasta,es_sede_local)
    VALUES (eid_origen,'Establecimiento de prueba '||lpad(i::text,2,'0'),'Red Lima Este','Convenio sintético de intercambio',
      date '2026-01-01',date '2027-12-31',false)
    ON CONFLICT DO NOTHING;
    SELECT id INTO eid_origen FROM establecimientos_red WHERE nombre = 'Establecimiento de prueba '||lpad(i::text,2,'0');
    SELECT id INTO eid_destino FROM establecimientos_red WHERE nombre = 'Establecimiento de prueba '||lpad(((i % 60)+1)::text,2,'0');

    INSERT INTO solicitudes_interhospitalarias (id,establecimiento_origen_id,establecimiento_destino_id,grupo_abo,factor_rh,
      tipo_hemocomponente,cantidad,estado,es_devolucion,creado_en,respondido_en,motivo)
    VALUES (uuid_generate_v5(uuid_nil(),'banco50:interhospitalaria:'||i),eid_origen,eid_destino,abo,rh,'concentrado_hematies',
      1+(i%4),'aceptada',false,now()-(i||' hours')::interval,now()-(i||' hours')::interval+interval '20 minutes','Reposición de stock de prueba')
    ON CONFLICT DO NOTHING;

    INSERT INTO alertas (id,tipo,referencia_id,titulo,mensaje,atendida,creado_en)
    VALUES (uuid_generate_v5(uuid_nil(),'banco50:alerta:'||i),CASE WHEN i%2=0 THEN 'amber' ELSE 'blue' END,bid,
      'Alerta sintética '||lpad(i::text,2,'0'),'Mensaje de validación para la unidad '||i,i%3=0,now()-(i||' hours')::interval)
    ON CONFLICT DO NOTHING;

    INSERT INTO auditoria_logs (id,usuario_id,accion,entidad,entidad_id,ip_terminal,detalle,creado_en)
    VALUES (uuid_generate_v5(uuid_nil(),'banco50:auditoria:'||i),uid,'Validación de datos sintéticos','unidades_sangre',bid,
      '10.20.4.'||(10+i),jsonb_build_object('origen','datos.sql','fila',i),now()-(i||' minutes')::interval)
    ON CONFLICT DO NOTHING;
  END LOOP;
END $$;

-- Los eventos importados de versiones antiguas solo guardaban el nombre del
-- paciente. Para normalizarlos se crea una ficha clínica sintética y se enlaza
-- cada evento; los nuevos eventos ya llegan con paciente_id desde la aplicación.
DO $$
DECLARE
    e record;
    n integer;
    v_paciente uuid;
BEGIN
    FOR e IN
        SELECT id, paciente, row_number() OVER (ORDER BY id) AS orden
          FROM eventos_hemovigilancia
         WHERE paciente_id IS NULL
    LOOP
        n := e.orden;
        v_paciente := uuid_generate_v5(uuid_nil(), 'hemovigilancia-paciente:' || e.id::text);
        INSERT INTO pacientes (id, dni, historia_clinica, nombres, fecha_nacimiento, sexo,
                               grupo_abo, factor_rh, peso_kg, altura_cm, hemoglobina_gdl,
                               telefono, correo, direccion)
        VALUES (v_paciente,
                (74000000 + n)::text,
                'HC-2026-' || lpad((90000 + n)::text, 5, '0'),
                coalesce(nullif(e.paciente, ''), 'Paciente histórico ' || n),
                date '1980-01-01' + n,
                CASE WHEN n % 2 = 0 THEN 'F' ELSE 'M' END,
                'O', 'positivo', 70, 170, 10,
                (920000000 + n)::text,
                'hemovigilancia' || n || '@example.test',
                'Registro histórico migrado')
        ON CONFLICT DO NOTHING;

        SELECT id INTO v_paciente
          FROM pacientes
         WHERE historia_clinica = 'HC-2026-' || lpad((90000 + n)::text, 5, '0');

        UPDATE eventos_hemovigilancia
           SET paciente_id = v_paciente,
               registro_legacy = false
         WHERE id = e.id;
    END LOOP;
END $$;

SELECT setval('numero_historia_local', greatest((SELECT max(right(historia_clinica,5)::integer)
  FROM pacientes WHERE historia_clinica ~ '^HC-[0-9]{4}-[0-9]{5}$'),1), true);

-- Nombres ficticios para las fichas de demostración.
DO $nombres_demo$
DECLARE
    i integer;
    nombre text;
    apellidos_demo text;
BEGIN
    FOR i IN 1..100 LOOP
        nombre := (ARRAY['Elena','Miguel','Rosa','Andrés','Clara','Javier','Lucía','Diego','Paula','Luis'])[1 + i % 10];
        apellidos_demo := (ARRAY['Quispe','Torres','Mendoza','Rojas','Salazar','Vargas','Paredes','Flores','Castro','Vega'])[1 + (i / 10) % 10] || ' Aguilar';
        UPDATE donantes SET nombres = nombre, apellidos = apellidos_demo
         WHERE dni = (72000000+i)::text AND apellidos = 'Sintético Prueba';
        UPDATE usuarios SET nombres = nombre, apellidos = apellidos_demo
         WHERE dni = (71000000+i)::text AND apellidos = 'Demostración'
           AND correo = 'usuario' || i || '@datos.hlev.gob.pe';
        UPDATE usuarios u
           SET correo = translate(lower(regexp_replace(trim(u.nombres || ' ' || u.apellidos),
                               '[[:space:]]+', '.', 'g')), 'áéíóúüñ', 'aeiouun') || '@example.test'
         WHERE u.dni = (71000000+i)::text AND u.correo = 'usuario' || i || '@datos.hlev.gob.pe';
        UPDATE pacientes SET nombres = nombre || ' ' || apellidos_demo
         WHERE dni = (73000000+i)::text AND nombres ~ '^Paciente [0-9]+ Sintético$';
        UPDATE pacientes SET nombres = nombre || ' ' || apellidos_demo
         WHERE correo = 'hemovigilancia' || i || '@example.test'
           AND direccion = 'Registro histórico migrado'
           AND (nombres ~ '^Paciente ([0-9]+ Sintético|histórico [0-9]+)$'
                OR nombres ~ ',[[:space:]]*[[:alpha:]]\.$');
    END LOOP;
    UPDATE eventos_hemovigilancia e SET paciente = p.nombres
      FROM pacientes p WHERE p.id = e.paciente_id
       AND (e.paciente ~ '^Paciente [0-9]+ Sintético$'
            OR (p.direccion = 'Registro histórico migrado'
                AND e.paciente ~ ',[[:space:]]*[[:alpha:]]\.$'));
END $nombres_demo$;
