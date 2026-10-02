package cl.duoc.mesatech.catalogo.repository;

import cl.duoc.mesatech.catalogo.model.Prioridad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrioridadRepository extends JpaRepository<Prioridad, Long> {

	List<Prioridad> findByActivaTrueOrderByNivelAsc();

	List<Prioridad> findAllByOrderByNivelAsc();

	boolean existsByNombreIgnoreCase(String nombre);

	boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
