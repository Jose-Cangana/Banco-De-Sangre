# ROL
Eres un desarrollador senior Java/Spring Boot. Vas a implementar la generación de reportes con JasperReports (Jaspersoft) en este proyecto.

# CONTEXTO DEL PROYECTO
- Repositorio con dos carpetas principales: `frontend/` y `mvc-springboot/`.
- Backend: Spring Boot (MVC) en `mvc-springboot/src/main/java/pe/edu/utp/bancosangre/` con los paquetes: config, controller, dao, db, model, service. Clase principal: `BancoSangreApplication.java`. Recursos en `src/main/resources`.
- Dependencias gestionadas con `mvc-springboot/pom.xml`.
- El script `bancosangre_bd.sql` en la raíz es SOLO de referencia para entender las tablas.

# REGLAS INQUEBRANTABLES
1. NO MODIFICAR LA BASE DE DATOS: nada de CREATE, ALTER, DROP, INSERT, UPDATE ni DELETE, ni cambios a `bancosangre_bd.sql`. Tampoco crear vistas, procedimientos ni tablas. Solo se permiten SELECT mediante las consultas que ya traen los .jrxml.
2. NO HACER NINGÚN COMMIT: prohibido `git commit`, `git push`, `git add`, `git stash`, crear ramas o cualquier comando que altere el historial o el índice de git. Deja los cambios solo en el working directory.
3. NO modificar las consultas SQL ni el diseño de los .jrxml. Si detectas un error en ellos, repórtalo al final, pero no lo corrijas.
4. No cambiar el comportamiento existente de la aplicación fuera de lo necesario para los reportes.

# ARCHIVOS QUE SE TE ENTREGAN
Tres archivos `.jrxml` (contienen la consulta y el estilo de cada reporte). Colócalos en `mvc-springboot/src/main/resources/reports/` (o la ruta de recursos que ya use el proyecto, si existe una convención).

# TAREAS (en este orden)

## Fase 1: Análisis (no escribas código todavía)
1. Recorre `frontend/` y `mvc-springboot/` y ubica TODOS los botones, enlaces o acciones de "Generar reporte" (o equivalentes: exportar, imprimir, PDF, etc.). Indica archivo y línea de cada uno.
2. Lee los 3 .jrxml e identifica: nombre del reporte, parámetros que esperan ($P{...}), campos y consulta SQL.
3. Relaciona cada botón con el .jrxml que le corresponde según el módulo/entidad.
4. Revisa cómo se obtiene la conexión a la BD (paquetes `db` y `config`) para reutilizarla; no crees una conexión nueva si ya existe un DataSource.
5. Revisa `pom.xml` para ver si ya existe la dependencia de JasperReports.
6. Presenta un resumen breve de lo encontrado y tu plan antes de continuar.

## Fase 2: Implementación
1. Agrega en `pom.xml` solo lo necesario de JasperReports (jasperreports, y fuentes/PDF si hace falta), compatible con la versión de Spring Boot y Java del proyecto.
2. Crea un `ReporteService` en el paquete `service` que: cargue el .jrxml desde el classpath, lo compile (y cachee el JasperReport compilado), llene el reporte con la conexión del DataSource y los parámetros recibidos, y exporte a PDF (byte[]).
3. Crea o amplía un controlador en `controller` con un endpoint por reporte que devuelva el PDF con `Content-Type: application/pdf` y `Content-Disposition` (inline o attachment, según como se vea mejor en el frontend). Recibe los parámetros del reporte por query params o los que el .jrxml requiera.
4. Conecta cada botón identificado en la Fase 1 con su endpoint, respetando el estilo y tecnología del frontend actual (no introduzcas frameworks nuevos).
5. Maneja errores (reporte no encontrado, parámetros inválidos, fallo de compilación) con mensajes claros y sin romper la app.
6. Mantén el código consistente con las convenciones del proyecto (nombres, capas, idioma de los identificadores).

## Fase 3: Verificación
1. Compila el proyecto (`mvn clean compile` o equivalente) y corrige errores que hayas introducido.
2. Si es posible, levanta la app y prueba cada endpoint. Si no puedes ejecutarla, indícalo.
3. Confirma explícitamente que no tocaste la BD y que no hiciste commits (`git status` solo para verificar, es de solo lectura).

# ENTREGABLE FINAL
Resumen con: lista de botones encontrados y a qué reporte quedaron conectados, archivos creados/modificados, endpoints nuevos, cómo probarlos y cualquier observación o problema encontrado en los .jrxml.