package com.rwcalle.springcloud.ms.items;

import java.time.Duration;

import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;

// Podemos eliminar este archivo y solo mantener el property: application.yml
// RESILIENCE4J - CONFIGURACION EN ARCHIVO YML

// @Configuration  // Deshabilitado: configuración movida a application.yml
public class AppConfig {

    @Bean
    Customizer<Resilience4JCircuitBreakerFactory> customizerCircuitBreaker(){
        return (factory) -> factory.configureDefault(id -> {
            return new Resilience4JConfigBuilder(id).circuitBreakerConfig(CircuitBreakerConfig
                .custom()
            .slidingWindowSize(10)  //Estado: CERRADO: Permite 10 llamadas
            .failureRateThreshold(50)   // Porcentaje: 50%
            .waitDurationInOpenState(Duration.ofSeconds(10L))   // Durante 10 segundos

            .permittedNumberOfCallsInHalfOpenState(5) // Estado: SEMI ABIERTO: Permite 5 llamadas en general 
                .slowCallDurationThreshold(Duration.ofSeconds(2L))
                .slowCallRateThreshold(50)
            .build())
            .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(3L)).build())
            .build();
        });
    }
}
