package cl.duoc.mesatech.bff.controller;

import cl.duoc.mesatech.bff.dto.SolicitudesV2Response;
import cl.duoc.mesatech.bff.security.UsuarioActual;
import cl.duoc.mesatech.bff.service.SolicitudesService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API pública v2 de solicitudes. Coexiste con v1 sin modificarla: v2 cambia la forma de la respuesta
 * ({version, total, solicitudes}) y agrega datos del catálogo y del SLA; v1 se mantiene intacta para no
 * romper a los clientes que ya la consumen. Detalle en SolicitudesService#listarMiasV2.
 */
@RestController
@RequestMapping("/v2/solicitudes")
public class SolicitudesV2Controller {

	private final SolicitudesService service;

	public SolicitudesV2Controller(SolicitudesService service) {
		this.service = service;
	}

	@GetMapping("/mias")
	@PreAuthorize("hasAuthority('SCOPE_Solicitudes.Access')")
	public SolicitudesV2Response listarMias(@AuthenticationPrincipal Jwt jwt) {
		return service.listarMiasV2(UsuarioActual.desde(jwt));
	}
}
