package cl.duoc.mesatech.bff.dto;

/**
 * Cuerpo enviado a PATCH /api/solicitudes/{id}/estado de ms-solicitudes.
 */
public record CambiarEstadoInterno(String estado, String detalle, String operador) {
}
