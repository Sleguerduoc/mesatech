package cl.duoc.mesatech.catalogo.controller;

import cl.duoc.mesatech.catalogo.dto.CatalogoResponse;
import cl.duoc.mesatech.catalogo.dto.CategoriaRequest;
import cl.duoc.mesatech.catalogo.dto.PrioridadRequest;
import cl.duoc.mesatech.catalogo.model.Categoria;
import cl.duoc.mesatech.catalogo.model.Prioridad;
import cl.duoc.mesatech.catalogo.service.CatalogoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
 * Endpoints internos: solo los consume el BFF (el servicio escucha en 127.0.0.1).
 */
@RestController
@RequestMapping("/api/catalogo")
public class CatalogoController {

	private final CatalogoService service;

	public CatalogoController(CatalogoService service) {
		this.service = service;
	}

	@GetMapping
	public CatalogoResponse obtenerCatalogo(@RequestParam(defaultValue = "false") boolean incluirInactivas) {
		return service.obtenerCatalogo(incluirInactivas);
	}

	// ---------- Categorías ----------

	@GetMapping("/categorias/{id}")
	public Categoria obtenerCategoria(@PathVariable Long id) {
		return service.obtenerCategoria(id);
	}

	@PostMapping("/categorias")
	public ResponseEntity<Categoria> crearCategoria(@Valid @RequestBody CategoriaRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(service.crearCategoria(request));
	}

	@PutMapping("/categorias/{id}")
	public Categoria actualizarCategoria(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) {
		return service.actualizarCategoria(id, request);
	}

	@DeleteMapping("/categorias/{id}")
	public ResponseEntity<Void> eliminarCategoria(@PathVariable Long id) {
		service.eliminarCategoria(id);
		return ResponseEntity.noContent().build();
	}

	// ---------- Prioridades ----------

	@GetMapping("/prioridades/{id}")
	public Prioridad obtenerPrioridad(@PathVariable Long id) {
		return service.obtenerPrioridad(id);
	}

	@PostMapping("/prioridades")
	public ResponseEntity<Prioridad> crearPrioridad(@Valid @RequestBody PrioridadRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(service.crearPrioridad(request));
	}

	@PutMapping("/prioridades/{id}")
	public Prioridad actualizarPrioridad(@PathVariable Long id, @Valid @RequestBody PrioridadRequest request) {
		return service.actualizarPrioridad(id, request);
	}

	@DeleteMapping("/prioridades/{id}")
	public ResponseEntity<Void> eliminarPrioridad(@PathVariable Long id) {
		service.eliminarPrioridad(id);
		return ResponseEntity.noContent().build();
	}
}
