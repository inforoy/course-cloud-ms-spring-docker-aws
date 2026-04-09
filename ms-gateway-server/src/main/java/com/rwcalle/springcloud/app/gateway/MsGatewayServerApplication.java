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
import org.springframework.web.servlet.function.ServerResponse;

@SpringBootApplication
public class MsGatewayServerApplication {

	public static void main(String[] args) {

		SpringApplication.run(MsGatewayServerApplication.class, args);
		
	}

	@Bean
	RouterFunction<ServerResponse> routeConfig() {
		return route("ms-products").route(path("/api/products/**"), http())
		.filter(lb("ms-products"))
		.filter(circuitBreaker(config -> config
			.setId("products")
			.setStatusCodes("500")
			.setFallbackPath("forward:/api/items/5")))
		.before(stripPrefix(2)).build();
	}

}
