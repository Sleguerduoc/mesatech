package cl.duoc.mesatech.catalogo.dto;

import cl.duoc.mesatech.catalogo.model.Categoria;

/**
 * Respuesta de creación: datos de la categoría más el evento publicado, para trazar la solicitud HTTP
 * con categoriaId y eventId (messageId en RabbitMQ).
 */
public record CategoriaCreadaResponse(Long id, String nombre, String descripcion, boolean activa, String eventId,
		boolean eventoConfirmado) {

	public static CategoriaCreadaResponse de(Categoria categoria, String eventId) {
		return new CategoriaCreadaResponse(categoria.getId(), categoria.getNombre(), categoria.getDescripcion(),
				categoria.isActiva(), eventId, true);
	}
}
