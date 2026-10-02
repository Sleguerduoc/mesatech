package cl.duoc.mesatech.bff.dto;

import java.time.LocalDateTime;

/**
 * Solicitud del contrato v2: todos los campos de v1 más datos del catálogo y cálculo de SLA.
 */
public record SolicitudV2Dto(
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
		String detalleAtencion,
		String categoriaNombre,
		String prioridadNombre,
		Integer slaHoras,
		long horasAbierta,
		boolean vencida) {
}
