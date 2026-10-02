package cl.duoc.mesatech.bff.dto;

/**
 * Cuerpo enviado a POST /api/solicitudes de ms-solicitudes (datos del solicitante tomados del token).
 */
public record CrearSolicitudInterna(
		String titulo,
		String descripcion,
		Long categoriaId,
		Long prioridadId,
		String solicitanteOid,
		String solicitanteEmail) {
}
