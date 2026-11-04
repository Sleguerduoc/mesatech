package cl.duoc.mesatech.catalogo.messaging;

import cl.duoc.mesatech.catalogo.model.Categoria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Publica categoria.creada en catalogo.exchange con entrega persistente y espera la confirmación del
 * broker. La confirmación solo indica que RabbitMQ aceptó el mensaje; no que Solicitudes lo guardó.
 */
@Component
public class CategoriaPublisher {

	private static final Logger log = LoggerFactory.getLogger(CategoriaPublisher.class);

	private final RabbitTemplate rabbitTemplate;
	private final String exchange;
	private final String routingKey;
	private final long timeoutSegundos;

	public CategoriaPublisher(RabbitTemplate rabbitTemplate,
			@Value("${app.rabbitmq.exchange}") String exchange,
			@Value("${app.rabbitmq.routing-key}") String routingKey,
			@Value("${app.rabbitmq.confirm-timeout-segundos:10}") long timeoutSegundos) {
		this.rabbitTemplate = rabbitTemplate;
		this.exchange = exchange;
		this.routingKey = routingKey;
		this.timeoutSegundos = timeoutSegundos;
	}

	public EventoCategoria publicarCreacion(Categoria categoria) {
		EventoCategoria evento = new EventoCategoria(UUID.randomUUID().toString(), EventoCategoria.CATEGORIA_CREADA,
				Instant.now().toString(), categoria.getId(), categoria.getNombre());
		CorrelationData correlacion = new CorrelationData(evento.eventId());

		log.info("Publicando evento {} eventId={} categoriaId={} nombre='{}' exchange={} routingKey={}",
				evento.tipoEvento(), evento.eventId(), evento.categoriaId(), evento.nombre(), exchange, routingKey);
		try {
			rabbitTemplate.convertAndSend(exchange, routingKey, evento, mensaje -> {
				mensaje.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
				mensaje.getMessageProperties().setMessageId(evento.eventId());
				mensaje.getMessageProperties().setType(evento.tipoEvento());
				return mensaje;
			}, correlacion);

			CorrelationData.Confirm confirmacion = correlacion.getFuture().get(timeoutSegundos, TimeUnit.SECONDS);
			if (!confirmacion.ack()) {
				throw new AmqpException("RabbitMQ respondió nack: " + confirmacion.reason());
			}
			ReturnedMessage retornado = correlacion.getReturned();
			if (retornado != null) {
				throw new AmqpException("Mensaje retornado sin ruta a una cola (" + retornado.getReplyCode() + " "
						+ retornado.getReplyText() + ")");
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw fallo(evento, "se interrumpió la espera de confirmación", e);
		} catch (ExecutionException | TimeoutException e) {
			throw fallo(evento, "sin confirmación del broker en " + timeoutSegundos + " s", e);
		} catch (AmqpException e) {
			throw fallo(evento, e.getMessage(), e);
		}

		log.info("Evento confirmado por RabbitMQ (ack) eventId={} categoriaId={}", evento.eventId(),
				evento.categoriaId());
		return evento;
	}

	private PublicacionFallidaException fallo(EventoCategoria evento, String motivo, Throwable causa) {
		log.error("Publicación NO confirmada eventId={} categoriaId={}: {}", evento.eventId(), evento.categoriaId(),
				motivo);
		return new PublicacionFallidaException(motivo, evento.categoriaId(), evento.eventId(), causa);
	}
}
