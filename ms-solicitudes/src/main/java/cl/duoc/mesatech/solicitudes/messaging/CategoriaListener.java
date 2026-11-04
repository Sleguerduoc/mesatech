package cl.duoc.mesatech.solicitudes.messaging;

import cl.duoc.mesatech.solicitudes.model.CategoriaLocal;
import cl.duoc.mesatech.solicitudes.service.CategoriaLocalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Consume solicitudes.catalogo.queue. En modo AUTO el ack se envía al retornar este método, es decir,
 * después de que la copia quedó guardada en RDS Solicitudes. Si guardar falla, la excepción se propaga y
 * el mensaje vuelve a la cola (salvo un evento con contenido inválido, que se rechaza sin reencolar).
 */
@Component
public class CategoriaListener {

	private static final Logger log = LoggerFactory.getLogger(CategoriaListener.class);

	private final CategoriaLocalService service;

	public CategoriaListener(CategoriaLocalService service) {
		this.service = service;
	}

	@RabbitListener(queues = "${app.rabbitmq.queue}")
	public void recibir(@Payload EventoCategoria evento,
			@Header(name = AmqpHeaders.MESSAGE_ID, required = false) String messageId) {
		log.info("Evento recibido messageId={} eventId={} tipo={} categoriaId={} nombre='{}'", messageId,
				evento.eventId(), evento.tipoEvento(), evento.categoriaId(), evento.nombre());

		CategoriaLocal guardada;
		try {
			guardada = service.guardarDesdeEvento(evento);
		} catch (IllegalArgumentException e) {
			// Mensaje inválido: reencolarlo provocaría un ciclo infinito; se rechaza sin reencolar y queda en el log
			log.error("Evento descartado por contenido inválido: {}", e.getMessage());
			throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
		}
		// Un error de base de datos se propaga tal cual: el mensaje se reencola y se reintenta

		log.info("Copia guardada en RDS Solicitudes: categorias_recibidas.categoria_id={} eventId={} (ack al retornar)",
				guardada.getCategoriaId(), guardada.getEventId());
	}
}
