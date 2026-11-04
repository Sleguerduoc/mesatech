package cl.duoc.mesatech.solicitudes.service;

import cl.duoc.mesatech.solicitudes.messaging.EventoCategoria;
import cl.duoc.mesatech.solicitudes.model.CategoriaLocal;
import cl.duoc.mesatech.solicitudes.repository.CategoriaLocalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoriaLocalService {

	private final CategoriaLocalRepository repository;

	public CategoriaLocalService(CategoriaLocalRepository repository) {
		this.repository = repository;
	}

	/**
	 * Guarda (o reemplaza, si el mensaje se vuelve a entregar) la copia con el mismo categoriaId de origen.
	 */
	@Transactional
	public CategoriaLocal guardarDesdeEvento(EventoCategoria evento) {
		if (evento.categoriaId() == null || evento.nombre() == null || evento.nombre().isBlank()) {
			throw new IllegalArgumentException("Evento sin categoriaId o nombre: eventId=" + evento.eventId());
		}
		CategoriaLocal copia = new CategoriaLocal(evento.categoriaId(), evento.nombre(), evento.eventId(),
				evento.fechaEvento(), LocalDateTime.now());
		return repository.save(copia);
	}

	@Transactional(readOnly = true)
	public List<CategoriaLocal> listar() {
		return repository.findAllByOrderByNombreAsc();
	}

	@Transactional(readOnly = true)
	public boolean existe(Long categoriaId) {
		return categoriaId != null && repository.existsById(categoriaId);
	}
}
