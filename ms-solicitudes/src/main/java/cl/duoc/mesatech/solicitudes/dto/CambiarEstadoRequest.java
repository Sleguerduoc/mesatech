package cl.duoc.mesatech.solicitudes.dto;

import cl.duoc.mesatech.solicitudes.model.EstadoSolicitud;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoRequest(
		@NotNull(message = "el estado es obligatorio")
		EstadoSolicitud estado,

		String detalle,

		String operador) {
}
