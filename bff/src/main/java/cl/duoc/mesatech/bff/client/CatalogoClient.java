package cl.duoc.mesatech.bff.client;

import cl.duoc.mesatech.bff.dto.CatalogoDto;
import cl.duoc.mesatech.bff.dto.CategoriaDto;
import cl.duoc.mesatech.bff.dto.CategoriaRequest;
import cl.duoc.mesatech.bff.dto.PrioridadDto;
import cl.duoc.mesatech.bff.dto.PrioridadRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Encapsula las llamadas a los endpoints internos /api/catalogo de ms-catalogo.
 */
@Component
public class CatalogoClient {

	private final RestClient client;

	public CatalogoClient(@Qualifier("catalogoRestClient") RestClient client) {
		this.client = client;
	}

	public CatalogoDto obtenerCatalogo(boolean incluirInactivas) {
		return client.get()
				.uri(uri -> uri.path("/api/catalogo").queryParam("incluirInactivas", incluirInactivas).build())
				.retrieve()
				.body(CatalogoDto.class);
	}

	// ---------- Categorías ----------

	public CategoriaDto obtenerCategoria(Long id) {
		return client.get()
				.uri("/api/catalogo/categorias/{id}", id)
				.retrieve()
				.body(CategoriaDto.class);
	}

	public CategoriaDto crearCategoria(CategoriaRequest request) {
		return client.post()
				.uri("/api/catalogo/categorias")
				.contentType(MediaType.APPLICATION_JSON)
				.body(request)
				.retrieve()
				.body(CategoriaDto.class);
	}

	public CategoriaDto actualizarCategoria(Long id, CategoriaRequest request) {
		return client.put()
				.uri("/api/catalogo/categorias/{id}", id)
				.contentType(MediaType.APPLICATION_JSON)
				.body(request)
				.retrieve()
				.body(CategoriaDto.class);
	}

	public void eliminarCategoria(Long id) {
		client.delete()
				.uri("/api/catalogo/categorias/{id}", id)
				.retrieve()
				.toBodilessEntity();
	}

	// ---------- Prioridades ----------

	public PrioridadDto obtenerPrioridad(Long id) {
		return client.get()
				.uri("/api/catalogo/prioridades/{id}", id)
				.retrieve()
				.body(PrioridadDto.class);
	}

	public PrioridadDto crearPrioridad(PrioridadRequest request) {
		return client.post()
				.uri("/api/catalogo/prioridades")
				.contentType(MediaType.APPLICATION_JSON)
				.body(request)
				.retrieve()
				.body(PrioridadDto.class);
	}

	public PrioridadDto actualizarPrioridad(Long id, PrioridadRequest request) {
		return client.put()
				.uri("/api/catalogo/prioridades/{id}", id)
				.contentType(MediaType.APPLICATION_JSON)
				.body(request)
				.retrieve()
				.body(PrioridadDto.class);
	}

	public void eliminarPrioridad(Long id) {
		client.delete()
				.uri("/api/catalogo/prioridades/{id}", id)
				.retrieve()
				.toBodilessEntity();
	}
}
