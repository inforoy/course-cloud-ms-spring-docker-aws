package com.rwcalle.springcloud.ms.users.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.rwcalle.springcloud.ms.users.entities.User;

public interface UserRepository extends CrudRepository<User, Long>{

    Optional<User> findByUsername(String username);

}
