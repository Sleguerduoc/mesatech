package cl.duoc.mesatech.catalogo.exception;

import cl.duoc.mesatech.catalogo.messaging.PublicacionFallidaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(RecursoNoEncontradoException.class)
	public ResponseEntity<Map<String, Object>> manejarNoEncontrado(RecursoNoEncontradoException ex) {
		return construirRespuesta(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(NombreDuplicadoException.class)
	public ResponseEntity<Map<String, Object>> manejarNombreDuplicado(NombreDuplicadoException ex) {
		return construirRespuesta(HttpStatus.CONFLICT, ex.getMessage());
	}

	/**
	 * La categoría quedó guardada en RDS, pero el broker no confirmó el evento. No hay rollback
	 * automático: se informa el resultado real para que el administrador lo revise.
	 */
	@ExceptionHandler(PublicacionFallidaException.class)
	public ResponseEntity<Map<String, Object>> manejarPublicacionFallida(PublicacionFallidaException ex) {
		ResponseEntity<Map<String, Object>> respuesta = construirRespuesta(HttpStatus.SERVICE_UNAVAILABLE,
				"La categoría " + ex.getCategoriaId() + " se guardó en el catálogo, pero no se pudo confirmar la "
						+ "publicación del evento en RabbitMQ (" + ex.getMessage() + "). Solicitudes no la recibirá "
						+ "hasta que se vuelva a publicar. No se revirtió el registro.");
		Map<String, Object> cuerpo = respuesta.getBody();
		cuerpo.put("categoriaId", ex.getCategoriaId());
		cuerpo.put("eventId", ex.getEventId());
		cuerpo.put("registroGuardado", true);
		cuerpo.put("eventoConfirmado", false);
		return respuesta;
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> manejarValidacion(MethodArgumentNotValidException ex) {
		String mensaje = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.collect(Collectors.joining("; "));
		return construirRespuesta(HttpStatus.BAD_REQUEST, mensaje);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, Object>> manejarCuerpoInvalido(HttpMessageNotReadableException ex) {
		return construirRespuesta(HttpStatus.BAD_REQUEST, "Cuerpo de la petición inválido");
	}

	private ResponseEntity<Map<String, Object>> construirRespuesta(HttpStatus status, String mensaje) {
		Map<String, Object> cuerpo = new LinkedHashMap<>();
		cuerpo.put("status", status.value());
		cuerpo.put("error", status.getReasonPhrase());
		cuerpo.put("mensaje", mensaje);
		cuerpo.put("timestamp", LocalDateTime.now().toString());
		return ResponseEntity.status(status).body(cuerpo);
	}
}
