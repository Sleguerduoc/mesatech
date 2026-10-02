package cl.duoc.mesatech.bff.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	private final RegistroPeticionesInterceptor registroPeticionesInterceptor;

	public WebConfig(RegistroPeticionesInterceptor registroPeticionesInterceptor) {
		this.registroPeticionesInterceptor = registroPeticionesInterceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(registroPeticionesInterceptor);
	}
}
