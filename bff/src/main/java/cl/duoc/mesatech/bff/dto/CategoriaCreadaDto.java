package cl.duoc.mesatech.bff.dto;

/**
 * Respuesta de ms-catalogo al crear una categoría: incluye el eventId publicado en RabbitMQ.
 */
public record CategoriaCreadaDto(Long id, String nombre, String descripcion, boolean activa, String eventId,
		boolean eventoConfirmado) {
}
