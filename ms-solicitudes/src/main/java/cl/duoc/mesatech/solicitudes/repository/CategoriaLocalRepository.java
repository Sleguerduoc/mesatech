package cl.duoc.mesatech.solicitudes.repository;

import cl.duoc.mesatech.solicitudes.model.CategoriaLocal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaLocalRepository extends JpaRepository<CategoriaLocal, Long> {

	List<CategoriaLocal> findAllByOrderByNombreAsc();
}
