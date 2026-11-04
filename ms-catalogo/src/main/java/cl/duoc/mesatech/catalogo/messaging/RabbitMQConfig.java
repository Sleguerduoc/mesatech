package cl.duoc.mesatech.catalogo.messaging;

import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * El exchange, la cola y el binding se crean en el broker (script de configuración de RabbitMQ);
 * aquí no se declaran para que el usuario publicador no necesite permisos de configuración.
 */
@Configuration
public class RabbitMQConfig {

	@Bean
	public MessageConverter jsonMessageConverter() {
		return new JacksonJsonMessageConverter();
	}
}
