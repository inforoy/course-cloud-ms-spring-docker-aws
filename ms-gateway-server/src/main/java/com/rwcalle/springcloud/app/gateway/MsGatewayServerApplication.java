package com.rwcalle.springcloud.app.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.stripPrefix;
import static org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions.circuitBreaker;
import static org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions.lb;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

@SpringBootApplication
public class MsGatewayServerApplication {

	// Se comenta el constructor y el campo porque generaban una dependencia circular:
	// Spring intenta inyectar el @Bean routeConfig() en el constructor de la misma clase
	// que lo define, causando que el contexto no levante correctamente.
	// private final RouterFunction<ServerResponse> routeConfig;
	// MsGatewayServerApplication(RouterFunction<ServerResponse> routeConfig) {
	//     this.routeConfig = routeConfig;
	// }

    public static void main(String[] args) {

		SpringApplication.run(MsGatewayServerApplication.class, args);
		
	}

	// Se comenta el Bean de Java DSL porque la ruta de ms-products
	// fue movida de vuelta al application.yml tras el downgrade a spring-cloud-gateway-mvc 2024.x.
	// El Java DSL del modulo 11 (video 135) usaba spring-cloud-gateway-server-webmvc 2025.x
	// que tenia diferente comportamiento de enrutamiento.
	// @Bean
	// RouterFunction<ServerResponse> routeConfig() {
	// 	return route("ms-products").route(path("/api/products/**").or(path("/api/products")), http())
	// 	.filter((request, next) -> {
	// 		ServerRequest requestModified = ServerRequest.from(request)
	// 			.header("message-request", "algun mensaje al request").build();
	// 		ServerResponse response = next.handle(requestModified);
	// 		response.headers().add("message-response", "algun mensaje al response");
	// 		return response;
	// 	})
	// 	.filter(lb("ms-products"))
	// 	.filter(circuitBreaker(config -> config
	// 		.setId("products")
	// 		.setStatusCodes("500")
	// 		.setFallbackPath("forward:/api/items/5")))
	// 	.before(stripPrefix(2)).build();
	// }

}
