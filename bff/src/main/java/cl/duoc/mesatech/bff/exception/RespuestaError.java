package cl.duoc.mesatech.bff.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Construye el cuerpo de error estándar {status, error, mensaje, timestamp} usado en toda la plataforma.
 */
public final class RespuestaError {

	private RespuestaError() {
	}

	public static Map<String, Object> cuerpo(HttpStatus status, String mensaje) {
		Map<String, Object> cuerpo = new LinkedHashMap<>();
		cuerpo.put("status", status.value());
		cuerpo.put("error", status.getReasonPhrase());
		cuerpo.put("mensaje", mensaje);
		cuerpo.put("timestamp", LocalDateTime.now().toString());
		return cuerpo;
	}

	/**
	 * Versión en texto JSON para los filtros de seguridad (401/403), que escriben directo en la respuesta
	 * sin pasar por los convertidores de Spring MVC.
	 */
	public static String json(HttpStatus status, String mensaje) {
		return "{\"status\":" + status.value()
				+ ",\"error\":\"" + escapar(status.getReasonPhrase())
				+ "\",\"mensaje\":\"" + escapar(mensaje)
				+ "\",\"timestamp\":\"" + LocalDateTime.now() + "\"}";
	}

	private static String escapar(String texto) {
		return texto.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
