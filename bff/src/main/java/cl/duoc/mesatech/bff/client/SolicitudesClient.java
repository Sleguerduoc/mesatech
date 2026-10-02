package cl.duoc.mesatech.bff.client;

import cl.duoc.mesatech.bff.dto.CambiarEstadoInterno;
import cl.duoc.mesatech.bff.dto.CrearSolicitudInterna;
import cl.duoc.mesatech.bff.dto.SolicitudDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Encapsula las llamadas a los endpoints internos /api/solicitudes de ms-solicitudes.
 */
@Component
public class SolicitudesClient {

	private static final ParameterizedTypeReference<List<SolicitudDto>> LISTA = new ParameterizedTypeReference<>() {
	};

	private final RestClient client;

	public SolicitudesClient(@Qualifier("solicitudesRestClient") RestClient client) {
		this.client = client;
	}

	public SolicitudDto crear(CrearSolicitudInterna solicitud) {
		return client.post()
				.uri("/api/solicitudes")
				.contentType(MediaType.APPLICATION_JSON)
				.body(solicitud)
				.retrieve()
				.body(SolicitudDto.class);
	}

	public List<SolicitudDto> listarTodas() {
		return client.get()
				.uri("/api/solicitudes")
				.retrieve()
				.body(LISTA);
	}

	public List<SolicitudDto> listarPorSolicitante(String solicitanteOid) {
		return client.get()
				.uri(uri -> uri.path("/api/solicitudes").queryParam("solicitanteOid", solicitanteOid).build())
				.retrieve()
				.body(LISTA);
	}

	public SolicitudDto obtener(Long id) {
		return client.get()
				.uri("/api/solicitudes/{id}", id)
				.retrieve()
				.body(SolicitudDto.class);
	}

	public SolicitudDto cambiarEstado(Long id, CambiarEstadoInterno cambio) {
		return client.patch()
				.uri("/api/solicitudes/{id}/estado", id)
				.contentType(MediaType.APPLICATION_JSON)
				.body(cambio)
				.retrieve()
				.body(SolicitudDto.class);
	}
}
