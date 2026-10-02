package cl.duoc.mesatech.bff.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo que envía el frontend para cambiar el estado. El operador se toma del token.
 * El valor de estado lo valida ms-solicitudes (400 si es desconocido, 409 si la transición no está permitida).
 */
public record CambiarEstadoRequest(
		@NotBlank(message = "el estado es obligatorio")
		String estado,

		String detalle) {
}
