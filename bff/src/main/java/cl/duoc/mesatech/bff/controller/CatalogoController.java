package cl.duoc.mesatech.bff.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cl.duoc.mesatech.bff.client.CatalogoClient;
import cl.duoc.mesatech.bff.dto.CatalogoDto;
import cl.duoc.mesatech.bff.dto.CategoriaCreadaDto;
import cl.duoc.mesatech.bff.dto.CategoriaDto;
import cl.duoc.mesatech.bff.dto.CategoriaRequest;
import cl.duoc.mesatech.bff.dto.PrioridadDto;
import cl.duoc.mesatech.bff.dto.PrioridadRequest;
import cl.duoc.mesatech.bff.security.UsuarioActual;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API pública v1 del catálogo. Lectura para cualquier usuario con el scope; administración solo para
 * ROLE_ADMINISTRADOR. Los DELETE son lógicos (ms-catalogo marca activa=false).
 */
@RestController
@RequestMapping("/v1/catalogo")
public class CatalogoController {

	private static final String ADMIN = "hasAuthority('SCOPE_Catalogo.Access') and hasAuthority('ROLE_ADMINISTRADOR')";

	private static final Logger log = LoggerFactory.getLogger(CatalogoController.class);

	private final CatalogoClient catalogoClient;

	public CatalogoController(CatalogoClient catalogoClient) {
		this.catalogoClient = catalogoClient;
	}

	/** incluirInactivas solo se respeta para administradores; el resto siempre ve solo registros activos. */
	@GetMapping
	@PreAuthorize("hasAuthority('SCOPE_Catalogo.Access')")
	public CatalogoDto obtenerCatalogo(@RequestParam(defaultValue = "false") boolean incluirInactivas,
			@AuthenticationPrincipal Jwt jwt) {
		boolean incluir = incluirInactivas && UsuarioActual.desde(jwt).esAdministrador();
		return catalogoClient.obtenerCatalogo(incluir);
	}

	// ---------- Categorías ----------

	@PostMapping("/categorias")
	@PreAuthorize(ADMIN)
	public ResponseEntity<CategoriaCreadaDto> crearCategoria(@Valid @RequestBody CategoriaRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		CategoriaCreadaDto creada = catalogoClient.crearCategoria(request);
		log.info("Categoría creada id={} eventId={} por {}", creada.id(), creada.eventId(),
				UsuarioActual.desde(jwt).usuario());
		return ResponseEntity.status(HttpStatus.CREATED).body(creada);
	}

	@PutMapping("/categorias/{id}")
	@PreAuthorize(ADMIN)
	public CategoriaDto actualizarCategoria(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) {
		return catalogoClient.actualizarCategoria(id, request);
	}

	@DeleteMapping("/categorias/{id}")
	@PreAuthorize(ADMIN)
	public ResponseEntity<Void> eliminarCategoria(@PathVariable Long id) {
		catalogoClient.eliminarCategoria(id);
		return ResponseEntity.noContent().build();
	}

	// ---------- Prioridades ----------

	@PostMapping("/prioridades")
	@PreAuthorize(ADMIN)
	public ResponseEntity<PrioridadDto> crearPrioridad(@Valid @RequestBody PrioridadRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(catalogoClient.crearPrioridad(request));
	}

	@PutMapping("/prioridades/{id}")
	@PreAuthorize(ADMIN)
	public PrioridadDto actualizarPrioridad(@PathVariable Long id, @Valid @RequestBody PrioridadRequest request) {
		return catalogoClient.actualizarPrioridad(id, request);
	}

	@DeleteMapping("/prioridades/{id}")
	@PreAuthorize(ADMIN)
	public ResponseEntity<Void> eliminarPrioridad(@PathVariable Long id) {
		catalogoClient.eliminarPrioridad(id);
		return ResponseEntity.noContent().build();
	}
}
