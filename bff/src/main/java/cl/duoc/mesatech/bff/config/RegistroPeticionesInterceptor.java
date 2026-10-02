package cl.duoc.mesatech.bff.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Registra a nivel INFO cada operación que llega al BFF: método, ruta, oid del usuario y status.
 * Nunca imprime el token. Sirve como evidencia de que las peticiones rechazadas por API Gateway
 * no llegan al BFF (no aparecerán en este log).
 */
@Component
public class RegistroPeticionesInterceptor implements HandlerInterceptor {

	private static final Logger log = LoggerFactory.getLogger(RegistroPeticionesInterceptor.class);
	private static final String INICIO = RegistroPeticionesInterceptor.class.getName() + ".inicio";

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		request.setAttribute(INICIO, System.currentTimeMillis());
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		Object inicio = request.getAttribute(INICIO);
		long duracion = inicio instanceof Long ms ? System.currentTimeMillis() - ms : -1;
		log.info("{} {} oid={} status={} ({} ms)", request.getMethod(), request.getRequestURI(), oidActual(),
				response.getStatus(), duracion);
	}

	private String oidActual() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth instanceof JwtAuthenticationToken jwtAuth) {
			return jwtAuth.getToken().getClaimAsString("oid");
		}
		return "anónimo";
	}
}
