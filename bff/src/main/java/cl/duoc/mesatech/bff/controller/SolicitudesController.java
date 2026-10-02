package cl.duoc.mesatech.bff.controller;

import cl.duoc.mesatech.bff.dto.CambiarEstadoRequest;
import cl.duoc.mesatech.bff.dto.CrearSolicitudRequest;
import cl.duoc.mesatech.bff.dto.SolicitudDto;
import cl.duoc.mesatech.bff.security.UsuarioActual;
import cl.duoc.mesatech.bff.service.SolicitudesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/solicitudes")
public class SolicitudesController {

	private final SolicitudesService service;

	public SolicitudesController(SolicitudesService service) {
		this.service = service;
	}

	@PostMapping
	@PreAuthorize("hasAuthority('SCOPE_Solicitudes.Access') and hasAnyAuthority('ROLE_CLIENTE','ROLE_OPERADOR','ROLE_ADMINISTRADOR')")
	public ResponseEntity<SolicitudDto> crear(@Valid @RequestBody CrearSolicitudRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request, UsuarioActual.desde(jwt)));
	}

	@GetMapping("/mias")
	@PreAuthorize("hasAuthority('SCOPE_Solicitudes.Access')")
	public List<SolicitudDto> listarMias(@AuthenticationPrincipal Jwt jwt) {
		return service.listarMias(UsuarioActual.desde(jwt));
	}

	@GetMapping
	@PreAuthorize("hasAuthority('SCOPE_Solicitudes.Access') and hasAnyAuthority('ROLE_OPERADOR','ROLE_ADMINISTRADOR')")
	public List<SolicitudDto> listarTodas() {
		return service.listarTodas();
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAuthority('SCOPE_Solicitudes.Access')")
	public SolicitudDto obtener(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
		return service.obtener(id, UsuarioActual.desde(jwt));
	}

	@PatchMapping("/{id}/estado")
	@PreAuthorize("hasAuthority('SCOPE_Solicitudes.Access') and hasAnyAuthority('ROLE_OPERADOR','ROLE_ADMINISTRADOR')")
	public SolicitudDto cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambiarEstadoRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return service.cambiarEstado(id, request, UsuarioActual.desde(jwt));
	}
}
