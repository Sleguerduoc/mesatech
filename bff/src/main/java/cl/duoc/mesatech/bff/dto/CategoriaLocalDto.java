package cl.duoc.mesatech.bff.dto;

import java.time.LocalDateTime;

/**
 * Categoría disponible en la copia local de ms-solicitudes (recibida por RabbitMQ).
 */
public record CategoriaLocalDto(Long categoriaId, String nombre, String eventId, String fechaEvento,
		LocalDateTime fechaRecepcion) {
}
