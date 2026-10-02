package cl.duoc.mesatech.bff.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Arrays;
import java.util.List;

/**
 * Datos del usuario autenticado extraídos del access token de Entra ID ya validado por Spring Security.
 */
public record UsuarioActual(
		String oid,
		String nombre,
		String usuario,
		String email,
		List<String> roles,
		List<String> scopes) {

	public static final String ROL_CLIENTE = "ROLE_CLIENTE";
	public static final String ROL_OPERADOR = "ROLE_OPERADOR";
	public static final String ROL_ADMINISTRADOR = "ROLE_ADMINISTRADOR";

	public static UsuarioActual desde(Jwt jwt) {
		String usuario = primerNoVacio(jwt.getClaimAsString("preferred_username"),
				jwt.getClaimAsString("upn"), jwt.getClaimAsString("email"));
		String email = primerNoVacio(jwt.getClaimAsString("email"), usuario);

		List<String> roles = jwt.getClaimAsStringList("roles");
		String scp = jwt.getClaimAsString("scp");
		List<String> scopes = (scp == null || scp.isBlank())
				? List.of()
				: Arrays.asList(scp.trim().split("\\s+"));

		return new UsuarioActual(jwt.getClaimAsString("oid"), jwt.getClaimAsString("name"), usuario, email,
				roles == null ? List.of() : List.copyOf(roles), List.copyOf(scopes));
	}

	public boolean esAdministrador() {
		return roles.contains(ROL_ADMINISTRADOR);
	}

	/** Operadores y administradores pueden ver y gestionar todas las solicitudes. */
	public boolean esOperadorOAdministrador() {
		return roles.contains(ROL_OPERADOR) || esAdministrador();
	}

	private static String primerNoVacio(String... valores) {
		for (String valor : valores) {
			if (valor != null && !valor.isBlank()) {
				return valor;
			}
		}
		return null;
	}
}
