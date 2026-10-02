package cl.duoc.mesatech.catalogo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PrioridadRequest(
		@NotBlank(message = "el nombre es obligatorio")
		@Size(max = 50, message = "el nombre no puede superar 50 caracteres")
		String nombre,

		@NotNull(message = "el nivel es obligatorio")
		@Min(value = 1, message = "el nivel debe ser mayor o igual a 1")
		Integer nivel,

		@NotNull(message = "las horas de SLA son obligatorias")
		@Min(value = 1, message = "las horas de SLA deben ser mayores o iguales a 1")
		Integer slaHoras) {
}
