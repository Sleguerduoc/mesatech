package cl.duoc.mesatech.catalogo.dto;

import cl.duoc.mesatech.catalogo.model.Categoria;
import cl.duoc.mesatech.catalogo.model.Prioridad;

import java.util.List;

public record CatalogoResponse(List<Categoria> categorias, List<Prioridad> prioridades) {
}
