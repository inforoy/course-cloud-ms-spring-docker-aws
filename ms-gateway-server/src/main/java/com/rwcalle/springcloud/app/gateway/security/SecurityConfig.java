package com.rwcalle.springcloud.app.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http){
        return http.authorizeExchange(authz -> {
            authz.pathMatchers("/authorized","logout").permitAll()
            .pathMatchers(HttpMethod.GET, "/api/items","/api/products","/api/users").permitAll()
            .pathMatchers(HttpMethod.GET, "/api/items/{id}","/api/products/{id}","/api/users/{id}").hasAnyRole("ADMIN", "USER")
            .pathMatchers("/api/items/**","/api/products/**","/api/users/**").hasRole("ADMIN")
            .anyExchange().authenticated();
        }).cors(csrf -> csrf.disable())
        .oauth2Login(withDefaults())
        .oauth2Client(withDefaults())
        .oauth2ResourceServer(withDefaults())
        .build();
    }

}
