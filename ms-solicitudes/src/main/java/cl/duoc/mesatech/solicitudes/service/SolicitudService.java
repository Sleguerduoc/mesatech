package cl.duoc.mesatech.solicitudes.service;

import cl.duoc.mesatech.solicitudes.dto.CambiarEstadoRequest;
import cl.duoc.mesatech.solicitudes.dto.CrearSolicitudRequest;
import cl.duoc.mesatech.solicitudes.exception.RecursoNoEncontradoException;
import cl.duoc.mesatech.solicitudes.exception.TransicionInvalidaException;
import cl.duoc.mesatech.solicitudes.model.EstadoSolicitud;
import cl.duoc.mesatech.solicitudes.model.Solicitud;
import cl.duoc.mesatech.solicitudes.repository.SolicitudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SolicitudService {

	private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private final SolicitudRepository repository;

	public SolicitudService(SolicitudRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public Solicitud crear(CrearSolicitudRequest request) {
		Solicitud solicitud = new Solicitud();
		solicitud.setTitulo(request.titulo());
		solicitud.setDescripcion(request.descripcion());
		solicitud.setCategoriaId(request.categoriaId());
		solicitud.setPrioridadId(request.prioridadId());
		solicitud.setSolicitanteOid(request.solicitanteOid());
		solicitud.setSolicitanteEmail(request.solicitanteEmail());
		return repository.save(solicitud);
	}

	@Transactional(readOnly = true)
	public List<Solicitud> listar(String solicitanteOid) {
		if (solicitanteOid == null || solicitanteOid.isBlank()) {
			return repository.findAllByOrderByFechaCreacionDesc();
		}
		return repository.findBySolicitanteOidOrderByFechaCreacionDesc(solicitanteOid);
	}

	@Transactional(readOnly = true)
	public Solicitud obtener(Long id) {
		return repository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe la solicitud con id " + id));
	}

	@Transactional
	public Solicitud cambiarEstado(Long id, CambiarEstadoRequest request) {
		Solicitud solicitud = obtener(id);
		EstadoSolicitud actual = solicitud.getEstado();
		EstadoSolicitud nuevo = request.estado();

		if (!actual.puedePasarA(nuevo)) {
			if (nuevo == EstadoSolicitud.RESUELTA) {
				throw new TransicionInvalidaException(
						"Una solicitud solo puede pasar a RESUELTA si está EN_PROCESO (estado actual: " + actual + ")");
			}
			throw new TransicionInvalidaException("Transición no permitida: " + actual + " -> " + nuevo);
		}

		LocalDateTime ahora = LocalDateTime.now();
		solicitud.setEstado(nuevo);

		if (nuevo == EstadoSolicitud.ASIGNADA && request.operador() != null && !request.operador().isBlank()) {
			solicitud.setOperadorAsignado(request.operador());
		}

		if (request.detalle() != null && !request.detalle().isBlank()) {
			String entrada = "[" + ahora.format(FORMATO_FECHA) + "] " + nuevo + ": " + request.detalle();
			String previo = solicitud.getDetalleAtencion();
			solicitud.setDetalleAtencion(previo == null || previo.isEmpty() ? entrada : previo + "\n" + entrada);
		}

		solicitud.setFechaActualizacion(ahora);
		return repository.save(solicitud);
	}
}
