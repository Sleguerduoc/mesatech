package cl.duoc.mesatech.solicitudes.controller;

import cl.duoc.mesatech.solicitudes.dto.CambiarEstadoRequest;
import cl.duoc.mesatech.solicitudes.dto.CrearSolicitudRequest;
import cl.duoc.mesatech.solicitudes.model.Solicitud;
import cl.duoc.mesatech.solicitudes.service.SolicitudService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints internos: solo los consume el BFF (el security group de la EC2 B solo admite su origen).
 */
@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudController {

	private final SolicitudService service;

	public SolicitudController(SolicitudService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<Solicitud> crear(@Valid @RequestBody CrearSolicitudRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
	}

	@GetMapping
	public List<Solicitud> listar(@RequestParam(required = false) String solicitanteOid) {
		return service.listar(solicitanteOid);
	}

	@GetMapping("/{id}")
	public Solicitud obtener(@PathVariable Long id) {
		return service.obtener(id);
	}

	@PatchMapping("/{id}/estado")
	public Solicitud cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambiarEstadoRequest request) {
		return service.cambiarEstado(id, request);
	}
}
