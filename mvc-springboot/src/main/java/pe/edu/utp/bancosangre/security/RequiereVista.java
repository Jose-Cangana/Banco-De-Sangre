package pe.edu.utp.bancosangre.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca un endpoint que exige sesion iniciada y que el rol tenga acceso a AL MENOS UNA de las
 * vistas indicadas (ver {@link pe.edu.utp.bancosangre.model.Rol#puedeVer(String)}).
 * Lo aplica {@link PermisosInterceptor}: sin sesion responde 401; sin permiso, 403.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RequiereVista {
    String[] value();
}
