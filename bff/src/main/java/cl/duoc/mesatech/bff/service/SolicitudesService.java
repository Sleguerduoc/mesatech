package cl.duoc.mesatech.bff.service;

import cl.duoc.mesatech.bff.client.CatalogoClient;
import cl.duoc.mesatech.bff.client.SolicitudesClient;
import cl.duoc.mesatech.bff.dto.CambiarEstadoInterno;
import cl.duoc.mesatech.bff.dto.CambiarEstadoRequest;
import cl.duoc.mesatech.bff.dto.CatalogoDto;
import cl.duoc.mesatech.bff.dto.CategoriaLocalDto;
import cl.duoc.mesatech.bff.dto.CategoriaDto;
import cl.duoc.mesatech.bff.dto.CrearSolicitudInterna;
import cl.duoc.mesatech.bff.dto.CrearSolicitudRequest;
import cl.duoc.mesatech.bff.dto.PrioridadDto;
import cl.duoc.mesatech.bff.dto.SolicitudDto;
import cl.duoc.mesatech.bff.dto.SolicitudV2Dto;
import cl.duoc.mesatech.bff.dto.SolicitudesV2Response;
import cl.duoc.mesatech.bff.exception.PeticionInvalidaException;
import cl.duoc.mesatech.bff.security.UsuarioActual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Orquesta las operaciones de solicitudes: aplica reglas de autorización por dueño, toma los datos del
 * solicitante desde el token y combina información de ms-solicitudes y ms-catalogo.
 */
@Service
public class SolicitudesService {

	private static final Logger log = LoggerFactory.getLogger(SolicitudesService.class);

	/** Estados en que la solicitud ya no está pendiente de atención (el SLA deja de correr). */
	private static final Set<String> ESTADOS_FINALES = Set.of("RESUELTA", "CERRADA", "CANCELADA");

	private final SolicitudesClient solicitudesClient;
	private final CatalogoClient catalogoClient;

	public SolicitudesService(SolicitudesClient solicitudesClient, CatalogoClient catalogoClient) {
		this.solicitudesClient = solicitudesClient;
		this.catalogoClient = catalogoClient;
	}

	public SolicitudDto crear(CrearSolicitudRequest request, UsuarioActual usuario) {
		// La categoría la valida ms-solicitudes contra su copia local (EP2); aquí se valida la prioridad
		validarPrioridad(request.prioridadId());

		// El solicitante SIEMPRE sale del token validado; nunca del body enviado por el cliente
		CrearSolicitudInterna interna = new CrearSolicitudInterna(request.titulo(), request.descripcion(),
				request.categoriaId(), request.prioridadId(), usuario.oid(), usuario.email());
		SolicitudDto creada = solicitudesClient.crear(interna);
		log.info("Solicitud creada id={} por oid={}", creada.id(), usuario.oid());
		return creada;
	}

	public List<CategoriaLocalDto> categoriasDisponibles() {
		return solicitudesClient.listarCategoriasLocales();
	}

	public List<SolicitudDto> listarMias(UsuarioActual usuario) {
		return solicitudesClient.listarPorSolicitante(usuario.oid());
	}

	public List<SolicitudDto> listarTodas() {
		return solicitudesClient.listarTodas();
	}

	/** Un cliente solo puede ver sus propias solicitudes; operadores y administradores ven todas. */
	public SolicitudDto obtener(Long id, UsuarioActual usuario) {
		SolicitudDto solicitud = solicitudesClient.obtener(id);
		if (!usuario.esOperadorOAdministrador() && !solicitud.solicitanteOid().equals(usuario.oid())) {
			log.warn("oid={} intentó ver la solicitud id={} de otro usuario", usuario.oid(), id);
			throw new AccessDeniedException("No tiene permisos para esta operación");
		}
		return solicitud;
	}

	public SolicitudDto cambiarEstado(Long id, CambiarEstadoRequest request, UsuarioActual usuario) {
		// El operador que atiende se toma del token (usuario autenticado), no del body
		CambiarEstadoInterno cambio = new CambiarEstadoInterno(request.estado(), request.detalle(), usuario.usuario());
		SolicitudDto actualizada = solicitudesClient.cambiarEstado(id, cambio);
		log.info("Solicitud id={} pasó a {} por oid={}", id, actualizada.estado(), usuario.oid());
		return actualizada;
	}

	/**
	 * Versión 2 de "mis solicitudes".
	 *
	 * Qué cambió en v2: la respuesta ya no es una lista plana sino {version, total, solicitudes}, y cada
	 * solicitud se enriquece con el nombre de la categoría y prioridad, las horas de SLA, las horas que
	 * lleva abierta y si está vencida. Para eso el BFF orquesta ambos microservicios: obtiene el catálogo
	 * UNA sola vez (incluyendo inactivas, porque una solicitud antigua puede apuntar a un registro
	 * desactivado) y lo combina en memoria con las solicitudes.
	 *
	 * Por qué v1 se mantiene: cambiar la forma de la respuesta de /v1 rompería a los clientes existentes
	 * (frontend ya desplegado). v1 y v2 coexisten y cada cliente migra a v2 cuando esté listo.
	 */
	public SolicitudesV2Response listarMiasV2(UsuarioActual usuario) {
		List<SolicitudDto> solicitudes = solicitudesClient.listarPorSolicitante(usuario.oid());
		CatalogoDto catalogo = catalogoClient.obtenerCatalogo(true);

		Map<Long, CategoriaDto> categorias = catalogo.categorias().stream()
				.collect(Collectors.toMap(CategoriaDto::id, Function.identity()));
		Map<Long, PrioridadDto> prioridades = catalogo.prioridades().stream()
				.collect(Collectors.toMap(PrioridadDto::id, Function.identity()));

		LocalDateTime ahora = LocalDateTime.now();
		List<SolicitudV2Dto> enriquecidas = solicitudes.stream()
				.map(s -> enriquecer(s, categorias.get(s.categoriaId()), prioridades.get(s.prioridadId()), ahora))
				.toList();
		return new SolicitudesV2Response("v2", enriquecidas.size(), enriquecidas);
	}

	private SolicitudV2Dto enriquecer(SolicitudDto s, CategoriaDto categoria, PrioridadDto prioridad,
			LocalDateTime ahora) {
		boolean finalizada = ESTADOS_FINALES.contains(s.estado());
		// Si la solicitud ya terminó, las horas abiertas se cuentan hasta su última actualización
		LocalDateTime fin = finalizada && s.fechaActualizacion() != null ? s.fechaActualizacion() : ahora;
		long horasAbierta = s.fechaCreacion() == null ? 0 : Duration.between(s.fechaCreacion(), fin).toHours();
		Integer slaHoras = prioridad == null ? null : prioridad.slaHoras();
		boolean vencida = !finalizada && slaHoras != null && horasAbierta > slaHoras;

		return new SolicitudV2Dto(s.id(), s.titulo(), s.descripcion(), s.categoriaId(), s.prioridadId(),
				s.solicitanteOid(), s.solicitanteEmail(), s.estado(), s.fechaCreacion(), s.fechaActualizacion(),
				s.operadorAsignado(), s.detalleAtencion(),
				categoria == null ? null : categoria.nombre(),
				prioridad == null ? null : prioridad.nombre(),
				slaHoras, horasAbierta, vencida);
	}

	/** Verifica en ms-catalogo que la prioridad exista y esté activa; si no, 400. */
	private void validarPrioridad(Long prioridadId) {
		PrioridadDto prioridad;
		try {
			prioridad = catalogoClient.obtenerPrioridad(prioridadId);
		} catch (HttpClientErrorException.NotFound e) {
			prioridad = null;
		}
		if (prioridad == null || !prioridad.activa()) {
			throw new PeticionInvalidaException("La prioridad " + prioridadId + " no existe o no está activa");
		}
	}
}
