package cl.duoc.mesatech.bff.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Endpoint público de salud (sin token), útil para el health check de API Gateway / balanceador.
 */
@RestController
public class HealthController {

	@GetMapping("/health")
	public Map<String, String> health() {
		Map<String, String> estado = new LinkedHashMap<>();
		estado.put("status", "UP");
		estado.put("servicio", "bff");
		return estado;
	}
}
