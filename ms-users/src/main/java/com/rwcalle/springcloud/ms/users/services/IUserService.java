package com.rwcalle.springcloud.ms.users.services;

import java.util.Optional;

import com.rwcalle.springcloud.ms.users.entities.User;

public interface IUserService {

    Optional<User> findById(Long id);
    Optional<User> findByUsername(String usernameString);
    Iterable<User> findAll();
    User save(User user);
    void delete(Long id);

}
