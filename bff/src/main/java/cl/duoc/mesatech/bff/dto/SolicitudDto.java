package cl.duoc.mesatech.bff.dto;

import java.time.LocalDateTime;

/**
 * Solicitud tal como la devuelve ms-solicitudes (contrato v1).
 */
public record SolicitudDto(
		Long id,
		String titulo,
		String descripcion,
		Long categoriaId,
		Long prioridadId,
		String solicitanteOid,
		String solicitanteEmail,
		String estado,
		LocalDateTime fechaCreacion,
		LocalDateTime fechaActualizacion,
		String operadorAsignado,
		String detalleAtencion) {
}
