package com.rwcalle.springcloud.ms.users.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.rwcalle.springcloud.ms.users.entities.Role;

public interface RoleRepository extends CrudRepository<Role, Long> {

    Optional<Role> findByname(String name);

}
