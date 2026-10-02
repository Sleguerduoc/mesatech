package cl.duoc.mesatech.catalogo.config;

import cl.duoc.mesatech.catalogo.model.Categoria;
import cl.duoc.mesatech.catalogo.model.Prioridad;
import cl.duoc.mesatech.catalogo.repository.CategoriaRepository;
import cl.duoc.mesatech.catalogo.repository.PrioridadRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Carga el catálogo base al iniciar, solo si las tablas están vacías (no duplica datos en reinicios).
 */
@Component
public class DatosIniciales implements CommandLineRunner {

	private final CategoriaRepository categoriaRepository;
	private final PrioridadRepository prioridadRepository;

	public DatosIniciales(CategoriaRepository categoriaRepository, PrioridadRepository prioridadRepository) {
		this.categoriaRepository = categoriaRepository;
		this.prioridadRepository = prioridadRepository;
	}

	@Override
	public void run(String... args) {
		if (categoriaRepository.count() == 0) {
			categoriaRepository.saveAll(List.of(
					new Categoria("Hardware", "Problemas con equipos físicos: computadores, impresoras y periféricos"),
					new Categoria("Software", "Instalación, errores o actualización de aplicaciones"),
					new Categoria("Redes", "Conectividad a internet, red interna, Wi-Fi y VPN"),
					new Categoria("Cuentas y accesos", "Creación de cuentas, contraseñas y permisos de acceso"),
					new Categoria("Otro", "Solicitudes que no calzan en las demás categorías")));
		}

		if (prioridadRepository.count() == 0) {
			prioridadRepository.saveAll(List.of(
					new Prioridad("Baja", 1, 72),
					new Prioridad("Media", 2, 48),
					new Prioridad("Alta", 3, 24),
					new Prioridad("Crítica", 4, 4)));
		}
	}
}
