package cl.duoc.mesatech.bff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Clientes HTTP hacia los microservicios internos. Los timeouts evitan que el BFF quede bloqueado si
 * un microservicio no responde (se traduce a 503 en GlobalExceptionHandler).
 */
@Configuration
public class RestClientConfig {

	@Bean
	public RestClient solicitudesRestClient(@Value("${ms.solicitudes.url}") String url) {
		return crear(url);
	}

	@Bean
	public RestClient catalogoRestClient(@Value("${ms.catalogo.url}") String url) {
		return crear(url);
	}

	private RestClient crear(String baseUrl) {
		// java.net.http.HttpClient soporta PATCH (HttpURLConnection, usado por SimpleClientHttpRequestFactory, no).
		HttpClient httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(3))
				.build();
		JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
		factory.setReadTimeout(Duration.ofSeconds(10));
		return RestClient.builder()
				.baseUrl(baseUrl)
				.requestFactory(factory)
				.build();
	}
}
