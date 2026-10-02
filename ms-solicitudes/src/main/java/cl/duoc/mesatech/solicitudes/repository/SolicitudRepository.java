package cl.duoc.mesatech.solicitudes.repository;

import cl.duoc.mesatech.solicitudes.model.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

	List<Solicitud> findAllByOrderByFechaCreacionDesc();

	List<Solicitud> findBySolicitanteOidOrderByFechaCreacionDesc(String solicitanteOid);
}
