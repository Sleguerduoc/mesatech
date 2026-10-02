package cl.duoc.mesatech.catalogo.repository;

import cl.duoc.mesatech.catalogo.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

	List<Categoria> findByActivaTrueOrderByNombreAsc();

	List<Categoria> findAllByOrderByNombreAsc();

	boolean existsByNombreIgnoreCase(String nombre);

	boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
