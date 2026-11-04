package cl.duoc.mesatech.catalogo.messaging;

/**
 * Contrato del evento publicado al crear una categoría.
 *
 * eventId      String (UUID v4). También se envía como messageId de AMQP y como id de correlación.
 * tipoEvento   String, valor fijo "CATEGORIA_CREADA".
 * fechaEvento  String ISO-8601 en UTC, por ejemplo "2026-11-05T14:03:12.481Z".
 * categoriaId  Long, id original de la categoría en RDS Catálogo.
 * nombre       String, nombre de la categoría.
 */
public record EventoCategoria(String eventId, String tipoEvento, String fechaEvento, Long categoriaId,
		String nombre) {

	public static final String CATEGORIA_CREADA = "CATEGORIA_CREADA";
}
