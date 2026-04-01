package com.rwcalle.springcloud.app.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

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
        .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
        .oauth2Login(withDefaults())
        .oauth2Client(withDefaults())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()))
        .build();
    }

    /* 
    
    //La clase SecurityConfig para Spring MVC (Servlet)

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests((authz) -> {
            authz
                    .requestMatchers("/authorized", "/logout").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/products", "/api/items", "/api/users").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/products/{id}", "/api/items/{id}", "/api/users/{id}").hasAnyRole("ADMIN", "USER")
                    .requestMatchers("/api/products/**", "/api/items/**", "/api/users/**").hasRole("ADMIN")
                    .anyRequest().authenticated();
        })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .oauth2Login(login -> login.loginPage("/oauth2/authorization/client-app"))
                .oauth2Client(withDefaults())
                .oauth2ResourceServer(withDefaults())
                .build();
    }

    */
    

}
