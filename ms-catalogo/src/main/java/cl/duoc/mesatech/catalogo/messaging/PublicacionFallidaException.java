package cl.duoc.mesatech.catalogo.messaging;

/**
 * La categoría ya quedó guardada en RDS Catálogo, pero RabbitMQ no confirmó el evento.
 */
public class PublicacionFallidaException extends RuntimeException {

	private final Long categoriaId;
	private final String eventId;

	public PublicacionFallidaException(String mensaje, Long categoriaId, String eventId, Throwable causa) {
		super(mensaje, causa);
		this.categoriaId = categoriaId;
		this.eventId = eventId;
	}

	public Long getCategoriaId() {
		return categoriaId;
	}

	public String getEventId() {
		return eventId;
	}
}
