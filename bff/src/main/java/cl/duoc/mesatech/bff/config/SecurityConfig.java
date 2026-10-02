package cl.duoc.mesatech.bff.config;

import cl.duoc.mesatech.bff.exception.RespuestaError;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Segunda capa de validación: API Gateway ya validó el JWT, pero el BFF lo vuelve a validar
 * (firma, iss, aud y exp) y además decide la autorización por scopes y roles.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				// API sin estado con Bearer token: no hay cookies de sesión, por lo que CSRF no aplica
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				// CORS NO se configura aquí: lo resuelve AWS API Gateway. Configurarlo también en el BFF
				// duplicaría los headers Access-Control-* y el navegador rechazaría la respuesta.
				// .cors(Customizer.withDefaults())
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.GET, "/health").permitAll()
						.anyRequest().authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2
						.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
						.authenticationEntryPoint(authenticationEntryPoint())
						.accessDeniedHandler(accessDeniedHandler()))
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint(authenticationEntryPoint())
						.accessDeniedHandler(accessDeniedHandler()));
		return http.build();
	}

	/**
	 * Combina dos fuentes de autoridades del token de Entra ID:
	 * - claim "scp" (permisos delegados) -> SCOPE_xxx, con el convertidor por defecto.
	 * - claim "roles" (app roles) -> valor tal cual, sin prefijo (en Entra ya se llaman ROLE_CLIENTE, etc.).
	 */
	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtGrantedAuthoritiesConverter scopes = new JwtGrantedAuthoritiesConverter();

		JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
		roles.setAuthoritiesClaimName("roles");
		roles.setAuthorityPrefix("");

		Converter<Jwt, Collection<GrantedAuthority>> combinado = jwt -> {
			Collection<GrantedAuthority> autoridades = new ArrayList<>(scopes.convert(jwt));
			autoridades.addAll(roles.convert(jwt));
			return autoridades;
		};

		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(combinado);
		return converter;
	}

	/** 401: token ausente, con firma inválida, emisor/audiencia incorrectos o expirado. */
	@Bean
	public AuthenticationEntryPoint authenticationEntryPoint() {
		return (request, response, authException) -> {
			log.warn("401 {} {} - {}", request.getMethod(), request.getRequestURI(), authException.getMessage());
			response.setHeader("WWW-Authenticate", "Bearer");
			escribir(response, HttpStatus.UNAUTHORIZED, "Token ausente, inválido o expirado");
		};
	}

	/** 403: token válido pero sin las autoridades requeridas. */
	@Bean
	public AccessDeniedHandler accessDeniedHandler() {
		return (request, response, accessDeniedException) -> {
			log.warn("403 {} {}", request.getMethod(), request.getRequestURI());
			escribir(response, HttpStatus.FORBIDDEN, "No tiene permisos para esta operación");
		};
	}

	private static void escribir(HttpServletResponse response, HttpStatus status, String mensaje) throws IOException {
		response.setStatus(status.value());
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getWriter().write(RespuestaError.json(status, mensaje));
	}
}
