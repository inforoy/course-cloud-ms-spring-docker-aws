package com.rwcalle.springcloud.ms.users.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.rwcalle.springcloud.ms.users.entities.User;
import com.rwcalle.springcloud.ms.users.services.IUserService;

@RestController
//@RequestMapping("/users")
public class UserController {

    private final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private IUserService userService;
    
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        LOGGER.info("Llamada al controlador UserController::createUser(), creando usuario: {}", user);
        return new ResponseEntity<>(userService.save(user), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@RequestBody User user, @PathVariable Long id) {
        LOGGER.info("Llamada al controlador UserController::updateUser(), actualizando usuario: {}", user);
        return userService.update(user, id)
            .map(userUpdated -> ResponseEntity.status(HttpStatus.CREATED).body(userUpdated))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        LOGGER.info("Llamada al controlador UserController::getUserById(), buscando usuario con id: {}", id);
        return userService.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        LOGGER.info("Llamada al controlador UserController::getUserByUsername(), buscando usuario con username: {}", username);
        return userService.findByUsername(username).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Iterable<User>> getAllUsers() {
        LOGGER.info("Llamada al controlador UserController::getAllUsers(), listando usuarios");
        return ResponseEntity.ok(userService.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id){
        LOGGER.info("Llamada al controlador UserController::deleteUser(), eliminando usuario con id: {}", id);
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
}