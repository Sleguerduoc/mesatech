package cl.duoc.mesatech.bff.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	/**
	 * Error devuelto por un microservicio. Si es 4xx se reenvía el MISMO status y el MISMO cuerpo
	 * (por ejemplo, el 409 de transición de estado inválida llega intacto al frontend).
	 * Un 5xx interno se traduce a 502 para no exponer detalles del microservicio.
	 */
	@ExceptionHandler(RestClientResponseException.class)
	public ResponseEntity<?> manejarErrorMicroservicio(RestClientResponseException ex) {
		if (ex.getStatusCode().is4xxClientError()) {
			String cuerpo = ex.getResponseBodyAsString();
			if (cuerpo.isBlank()) {
				HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
				return ResponseEntity.status(status).body(RespuestaError.cuerpo(status, status.getReasonPhrase()));
			}
			return ResponseEntity.status(ex.getStatusCode()).contentType(MediaType.APPLICATION_JSON).body(cuerpo);
		}
		// 503 de ms-catalogo: la categoría se guardó pero el evento no se confirmó. Se reenvía el cuerpo
		// (categoriaId, eventId, registroGuardado) para que el frontend muestre el resultado real.
		String cuerpo5xx = ex.getResponseBodyAsString();
		if (ex.getStatusCode().value() == 503 && !cuerpo5xx.isBlank()) {
			log.error("Servicio interno respondió 503: {}", cuerpo5xx);
			return ResponseEntity.status(503).contentType(MediaType.APPLICATION_JSON).body(cuerpo5xx);
		}
		log.error("Error {} en servicio interno: {}", ex.getStatusCode().value(), ex.getMessage());
		return construirRespuesta(HttpStatus.BAD_GATEWAY, "Error en servicio interno");
	}

	@ExceptionHandler(ResourceAccessException.class)
	public ResponseEntity<?> manejarServicioNoDisponible(ResourceAccessException ex) {
		log.error("Servicio interno no disponible: {}", ex.getMessage());
		return construirRespuesta(HttpStatus.SERVICE_UNAVAILABLE, "Servicio interno no disponible");
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<?> manejarAccesoDenegado(AccessDeniedException ex) {
		return construirRespuesta(HttpStatus.FORBIDDEN, "No tiene permisos para esta operación");
	}

	@ExceptionHandler(PeticionInvalidaException.class)
	public ResponseEntity<?> manejarPeticionInvalida(PeticionInvalidaException ex) {
		return construirRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<?> manejarValidacion(MethodArgumentNotValidException ex) {
		String mensaje = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.collect(Collectors.joining("; "));
		return construirRespuesta(HttpStatus.BAD_REQUEST, mensaje);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<?> manejarCuerpoInvalido(HttpMessageNotReadableException ex) {
		return construirRespuesta(HttpStatus.BAD_REQUEST, "Cuerpo de la petición inválido");
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<?> manejarTipoInvalido(MethodArgumentTypeMismatchException ex) {
		return construirRespuesta(HttpStatus.BAD_REQUEST, "Valor inválido para el parámetro '" + ex.getName() + "'");
	}

	private ResponseEntity<?> construirRespuesta(HttpStatus status, String mensaje) {
		return ResponseEntity.status(status).body(RespuestaError.cuerpo(status, mensaje));
	}
}
