package com.rwcalle.springcloud.app.gateway.filters;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
//import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class SampleGlobalFilter implements GlobalFilter, Ordered {

    private final Logger LOGGER = LoggerFactory.getLogger(SampleGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        
        LOGGER.info("Ejecutnado el filtro antes del request PRE");

        // exchange.getRequest().mutate().headers(h -> h.add("token", "abcdef"));// Esto no se esta agregando?
        // Mutamos el exchange y lo guardamos en una variable
        ServerWebExchange mutaExchange = exchange.mutate()
        .request(r -> r.headers(h -> h.add("token", "abcdef")))
        .build();

        // Pasamos el mutaExchange al chain.filter
        return chain.filter(mutaExchange).then(Mono.fromRunnable(() -> {
            LOGGER.info("Ejecutando filtro POST response");
            
            String token = mutaExchange.getRequest().getHeaders().getFirst("token");
            LOGGER.info("token: " + token);
            if(token != null){
                LOGGER.info("token 1: " + token);
                mutaExchange.getResponse().getHeaders().add("token1", token);
            }

            Optional.ofNullable(mutaExchange.getRequest().getHeaders().getFirst("token"))
            .ifPresent(valueToken -> {
                LOGGER.info("token 2: " + valueToken);
                mutaExchange.getResponse().getHeaders().add("token2", valueToken);
            });

            mutaExchange.getResponse().getCookies().add("color", ResponseCookie.from("color", "red").build());
            //mutaExchange.getResponse().getHeaders().setContentType(MediaType.TEXT_PLAIN);

        }));
    }

    @Override
    public int getOrder() {
        return 100;
    }

}
