package cl.duoc.mesatech.bff.dto;

/**
 * Prioridad tal como la devuelve ms-catalogo.
 */
public record PrioridadDto(Long id, String nombre, Integer nivel, Integer slaHoras, boolean activa) {
}
