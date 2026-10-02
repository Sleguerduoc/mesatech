package cl.duoc.mesatech.solicitudes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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
		Long prioridadId,

		@NotBlank(message = "el identificador del solicitante es obligatorio")
		String solicitanteOid,

		String solicitanteEmail) {
}
