package cl.duoc.mesatech.bff.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo que envía el frontend. No incluye datos del solicitante: se toman SIEMPRE del token.
 */
public record CrearSolicitudRequest(
		@NotBlank(message = "el título es obligatorio")
		@Size(max = 150, message = "el título no puede superar 150 caracteres")
		String titulo,

		@NotBlank(message = "la descripción es obligatoria")
		@Size(max = 2000, message = "la descripción no puede superar 2000 caracteres")
		String descripcion,

		@NotNull(message = "la categoría es obligatoria")
		Long categoriaId,

		@NotNull(message = "la prioridad es obligatoria")
		Long prioridadId) {
}
