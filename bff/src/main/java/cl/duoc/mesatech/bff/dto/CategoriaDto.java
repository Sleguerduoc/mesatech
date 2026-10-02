package cl.duoc.mesatech.bff.dto;

/**
 * Categoría tal como la devuelve ms-catalogo.
 */
public record CategoriaDto(Long id, String nombre, String descripcion, boolean activa) {
}
