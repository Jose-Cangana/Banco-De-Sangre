package pe.edu.utp.bancosangre.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pe.edu.utp.bancosangre.model.Rol;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Qué colecciones puede enviar cada rol a /api/sincronizar. Cada pantalla envía las colecciones que cargó,
 * así que una colección se acepta si el rol puede ver AL MENOS UNA de las pantallas que la envían.
 * Las bitácoras de solo-agregar (auditNuevos, alertasNuevas, despachosNuevos) las puede enviar cualquier sesión.
 */
public final class PermisosColecciones {

    private static final ObjectMapper JSON = new ObjectMapper();

    private static final Map<String, List<String>> VISTAS_POR_COLECCION = Map.ofEntries(
            Map.entry("usuarios", List.of("administracion")),
            Map.entry("donantes", List.of("donantes", "inventario", "reportes")),
            Map.entry("inventory", List.of("inventario", "despacho", "donantes", "hemovigilancia", "solicitudes", "reportes")),
            Map.entry("serologia", List.of("donantes", "inventario")),
            Map.entry("pacientes", List.of("pacientes")),
            Map.entry("solicitudes", List.of("solicitudes", "despacho", "reportes")),
            Map.entry("pruebas", List.of("solicitudes")),
            Map.entry("hemovigilancia", List.of("hemovigilancia", "reportes")),
            Map.entry("intercambios", List.of("interhospitalario"))
    );

    private PermisosColecciones() { }

    /** Devuelve la primera colección del cuerpo que el rol NO puede escribir, o null si todas están permitidas. */
    public static String primeraNoPermitida(Rol rol, String cuerpoJson) {
        JsonNode raiz;
        try {
            raiz = JSON.readTree(cuerpoJson);
        } catch (Exception e) {
            return null; // el formato inválido lo rechaza ValidadorDatos
        }
        if (raiz == null || !raiz.isObject()) return null;
        for (Iterator<String> claves = raiz.fieldNames(); claves.hasNext(); ) {
            String clave = claves.next();
            List<String> vistas = VISTAS_POR_COLECCION.get(clave);
            if (vistas != null && vistas.stream().noneMatch(rol::puedeVer)) return clave;
        }
        return null;
    }
}
