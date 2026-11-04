package cl.duoc.mesatech.catalogo.config;

import cl.duoc.mesatech.catalogo.model.Prioridad;
import cl.duoc.mesatech.catalogo.repository.PrioridadRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Carga las prioridades base al iniciar, solo si la tabla está vacía (no duplica datos en reinicios).
 * Desde la EP2 las categorías no se precargan: se crean por la API para que cada una genere su evento
 * categoria.creada y llegue a la copia local de Solicitudes.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

	private final PrioridadRepository prioridadRepository;

	public DatosIniciales(PrioridadRepository prioridadRepository) {
		this.prioridadRepository = prioridadRepository;
	}

	@Override
	public void run(String... args) {
		if (prioridadRepository.count() == 0) {
			prioridadRepository.saveAll(List.of(
					new Prioridad("Baja", 1, 72),
					new Prioridad("Media", 2, 48),
					new Prioridad("Alta", 3, 24),
					new Prioridad("Crítica", 4, 4)));
		}
	}
}
