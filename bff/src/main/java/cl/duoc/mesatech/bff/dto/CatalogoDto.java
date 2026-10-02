package cl.duoc.mesatech.bff.dto;

import java.util.List;

/**
 * Catálogo completo tal como lo devuelve GET /api/catalogo de ms-catalogo.
 */
public record CatalogoDto(List<CategoriaDto> categorias, List<PrioridadDto> prioridades) {
}
