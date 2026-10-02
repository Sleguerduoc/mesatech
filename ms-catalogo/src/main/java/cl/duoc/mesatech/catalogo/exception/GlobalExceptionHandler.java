package cl.duoc.mesatech.catalogo.exception;

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
