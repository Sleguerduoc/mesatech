package cl.duoc.mesatech.solicitudes.messaging;

/**
 * Evento recibido desde Catálogo (mismo contrato que publica ms-catalogo).
 * fechaEvento: String ISO-8601 en UTC.
 */
public record EventoCategoria(String eventId, String tipoEvento, String fechaEvento, Long categoriaId,
		String nombre) {
}
