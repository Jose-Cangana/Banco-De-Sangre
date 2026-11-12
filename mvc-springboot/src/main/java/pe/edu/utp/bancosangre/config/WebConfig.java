package pe.edu.utp.bancosangre.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import pe.edu.utp.bancosangre.security.PermisosInterceptor;

/**
 * Redirige la raiz "/" a "/login" usando el registro oficial de Spring MVC
 * (ViewControllerRegistry) en lugar de un metodo @GetMapping("/") en un
 * @Controller normal. Este registro tiene prioridad garantizada sobre el
 * manejador de "welcome page" / recursos estaticos que trae Spring Boot por
 * defecto, evitando el 404 "No static resource ." al entrar por
 * http://localhost:8080 sin ruta.
 *
 * Tambien registra el interceptor de permisos por rol para todos los endpoints /api/**.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final PermisosInterceptor permisos;

    public WebConfig(PermisosInterceptor permisos) {
        this.permisos = permisos;
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/login");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permisos).addPathPatterns("/api/**");
    }
}
