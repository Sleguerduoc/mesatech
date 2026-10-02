package cl.duoc.mesatech.catalogo.service;

import cl.duoc.mesatech.catalogo.dto.CatalogoResponse;
import cl.duoc.mesatech.catalogo.dto.CategoriaRequest;
import cl.duoc.mesatech.catalogo.dto.PrioridadRequest;
import cl.duoc.mesatech.catalogo.exception.NombreDuplicadoException;
import cl.duoc.mesatech.catalogo.exception.RecursoNoEncontradoException;
import cl.duoc.mesatech.catalogo.model.Categoria;
import cl.duoc.mesatech.catalogo.model.Prioridad;
import cl.duoc.mesatech.catalogo.repository.CategoriaRepository;
import cl.duoc.mesatech.catalogo.repository.PrioridadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class CatalogoService {

	private final CategoriaRepository categoriaRepository;
	private final PrioridadRepository prioridadRepository;

	public CatalogoService(CategoriaRepository categoriaRepository, PrioridadRepository prioridadRepository) {
		this.categoriaRepository = categoriaRepository;
		this.prioridadRepository = prioridadRepository;
	}

	@Transactional(readOnly = true)
	public CatalogoResponse obtenerCatalogo(boolean incluirInactivas) {
		if (incluirInactivas) {
			return new CatalogoResponse(categoriaRepository.findAllByOrderByNombreAsc(),
					prioridadRepository.findAllByOrderByNivelAsc());
		}
		return new CatalogoResponse(categoriaRepository.findByActivaTrueOrderByNombreAsc(),
				prioridadRepository.findByActivaTrueOrderByNivelAsc());
	}

	// ---------- Categorías ----------

	@Transactional(readOnly = true)
	public Categoria obtenerCategoria(Long id) {
		return categoriaRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe la categoría con id " + id));
	}

	@Transactional
	public Categoria crearCategoria(CategoriaRequest request) {
		String nombre = request.nombre().trim();
		if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
			throw new NombreDuplicadoException("Ya existe una categoría con el nombre '" + nombre + "'");
		}
		return categoriaRepository.save(new Categoria(nombre, request.descripcion()));
	}

	@Transactional
	public Categoria actualizarCategoria(Long id, CategoriaRequest request) {
		Categoria categoria = obtenerCategoria(id);
		String nombre = request.nombre().trim();
		if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
			throw new NombreDuplicadoException("Ya existe otra categoría con el nombre '" + nombre + "'");
		}
		categoria.setNombre(nombre);
		categoria.setDescripcion(request.descripcion());
		return categoriaRepository.save(categoria);
	}

	/** Eliminación lógica: marca la categoría como inactiva (ver comentario de la clase). */
	@Transactional
	public void eliminarCategoria(Long id) {
		Categoria categoria = obtenerCategoria(id);
		categoria.setActiva(false);
		categoriaRepository.save(categoria);
	}

	// ---------- Prioridades ----------

	@Transactional(readOnly = true)
	public Prioridad obtenerPrioridad(Long id) {
		return prioridadRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe la prioridad con id " + id));
	}

	@Transactional
	public Prioridad crearPrioridad(PrioridadRequest request) {
		String nombre = request.nombre().trim();
		if (prioridadRepository.existsByNombreIgnoreCase(nombre)) {
			throw new NombreDuplicadoException("Ya existe una prioridad con el nombre '" + nombre + "'");
		}
		return prioridadRepository.save(new Prioridad(nombre, request.nivel(), request.slaHoras()));
	}

	@Transactional
	public Prioridad actualizarPrioridad(Long id, PrioridadRequest request) {
		Prioridad prioridad = obtenerPrioridad(id);
		String nombre = request.nombre().trim();
		if (prioridadRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
			throw new NombreDuplicadoException("Ya existe otra prioridad con el nombre '" + nombre + "'");
		}
		prioridad.setNombre(nombre);
		prioridad.setNivel(request.nivel());
		prioridad.setSlaHoras(request.slaHoras());
		return prioridadRepository.save(prioridad);
	}

	/** Eliminación lógica: marca la prioridad como inactiva (ver comentario de la clase). */
	@Transactional
	public void eliminarPrioridad(Long id) {
		Prioridad prioridad = obtenerPrioridad(id);
		prioridad.setActiva(false);
		prioridadRepository.save(prioridad);
	}
}
