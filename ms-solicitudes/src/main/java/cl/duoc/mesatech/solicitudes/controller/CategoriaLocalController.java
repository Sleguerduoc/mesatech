package cl.duoc.mesatech.solicitudes.controller;

import cl.duoc.mesatech.solicitudes.model.CategoriaLocal;
import cl.duoc.mesatech.solicitudes.service.CategoriaLocalService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoint interno (lo consume solo el BFF): categorías disponibles en la copia local de Solicitudes.
 */
@RestController
@RequestMapping("/api/categorias")
public class CategoriaLocalController {

	private final CategoriaLocalService service;

	public CategoriaLocalController(CategoriaLocalService service) {
		this.service = service;
	}

	@GetMapping
	public List<CategoriaLocal> listar() {
		return service.listar();
	}
}
