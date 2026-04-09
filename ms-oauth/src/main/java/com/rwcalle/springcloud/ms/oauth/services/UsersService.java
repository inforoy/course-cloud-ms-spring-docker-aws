package com.rwcalle.springcloud.ms.oauth.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.rwcalle.springcloud.ms.oauth.models.User;

@Service
public class UsersService implements UserDetailsService {

    private final Logger LOGGER = LoggerFactory.getLogger(UsersService.class);

    @Autowired
    private WebClient client;
    
    //@Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LOGGER.info("Iniciando el proceso de Login - Llamada al servicio UsersService::loadUserByUsername(), buscando usuario con username: {}", username);
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        try {
            User user = client.get().uri("/username/{username}", params)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(User.class)
            .block();

            List<GrantedAuthority> roles = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());

            LOGGER.info("Usuario '{}' encontrado en el sistema, roles: {}", username, roles);

            return new org.springframework.security.core.userdetails.User(
                user.getUsername(), 
                user.getPassword(), 
                user.isEnabled(), 
                true, 
                true, 
                true, 
                roles);
                
        } catch (Exception e) {
            String errorMessage = "Error en el login, no existe el usuario '" + username + "' en el sistema";
            LOGGER.error(errorMessage);
            throw new UsernameNotFoundException(errorMessage);
            //throw new RuntimeException("Error fetching user details", e);
        }
    	
    }
}
