package pe.edu.utp.bancosangre.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import pe.edu.utp.bancosangre.controller.AuthController;
import pe.edu.utp.bancosangre.model.Rol;

import java.util.Arrays;

/**
 * Control de acceso por rol (RBAC) centralizado: lee {@link RequiereVista} del metodo o de su
 * controlador y corta la peticion antes de llegar al endpoint si no hay sesion o permiso.
 */
@Component
public class PermisosInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod metodo)) return true;
        RequiereVista requisito = AnnotatedElementUtils.findMergedAnnotation(metodo.getMethod(), RequiereVista.class);
        if (requisito == null) {
            requisito = AnnotatedElementUtils.findMergedAnnotation(metodo.getBeanType(), RequiereVista.class);
        }
        if (requisito == null) return true;

        HttpSession sesion = request.getSession(false);
        Rol rol = sesion == null ? null : (Rol) sesion.getAttribute(AuthController.SESSION_ROL);
        if (rol == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        if (Arrays.stream(requisito.value()).noneMatch(rol::puedeVer)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }
}
