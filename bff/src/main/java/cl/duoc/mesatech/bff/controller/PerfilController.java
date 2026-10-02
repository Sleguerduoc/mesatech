package cl.duoc.mesatech.bff.controller;

import cl.duoc.mesatech.bff.security.UsuarioActual;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Devuelve los claims del token tal como los validó el BFF. Sirve como evidencia de la validación del JWT.
 */
@RestController
public class PerfilController {

	private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
			.withZone(ZoneId.systemDefault());

	@GetMapping("/v1/perfil")
	@PreAuthorize("isAuthenticated()")
	public Map<String, Object> perfil(@AuthenticationPrincipal Jwt jwt) {
		UsuarioActual usuario = UsuarioActual.desde(jwt);
		Map<String, Object> perfil = new LinkedHashMap<>();
		perfil.put("oid", usuario.oid());
		perfil.put("nombre", usuario.nombre());
		perfil.put("usuario", usuario.usuario());
		perfil.put("roles", usuario.roles());
		perfil.put("scopes", usuario.scopes());
		perfil.put("iss", jwt.getIssuer() == null ? null : jwt.getIssuer().toString());
		perfil.put("aud", jwt.getAudience());
		perfil.put("exp", jwt.getExpiresAt() == null ? null : FORMATO_FECHA.format(jwt.getExpiresAt()));
		return perfil;
	}
}
