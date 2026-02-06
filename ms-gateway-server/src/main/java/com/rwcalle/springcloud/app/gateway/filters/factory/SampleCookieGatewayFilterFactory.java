package com.rwcalle.springcloud.app.gateway.filters.factory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class SampleCookieGatewayFilterFactory extends AbstractGatewayFilterFactory<SampleCookieGatewayFilterFactory.ConfigurationCookie>{

    private final Logger LOGGER = LoggerFactory.getLogger(SampleCookieGatewayFilterFactory.class);

    public SampleCookieGatewayFilterFactory(){
        super(ConfigurationCookie.class);
    }

    @Override
    public GatewayFilter apply(ConfigurationCookie config) {

        GatewayFilter filter = (exchange, chain) -> {
            LOGGER.info("Ejecutando PRE Gateway Filter Factory: " + config.message);
            
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                Optional.ofNullable(config.value).ifPresent(cookie -> {
                    exchange.getResponse().addCookie(ResponseCookie.from(config.name, cookie).build());
                });
                LOGGER.info("Ejecutando POST Gateway Filter Factory: " + config.message);
            }));
        };

        return new OrderedGatewayFilter(filter, 100);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("message", "name", "value");
    }



    @Override
    public String name() {
        return "EjemploCookie";
    }



    public static class ConfigurationCookie {
        private String name;
        private String value;
        private String message;
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }
        public String getMessage() {
            return message;
        }
        public void setMessage(String message) {
            this.message = message;
        }
    }

}
