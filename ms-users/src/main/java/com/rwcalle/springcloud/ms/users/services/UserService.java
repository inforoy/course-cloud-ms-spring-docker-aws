package com.rwcalle.springcloud.ms.users.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rwcalle.springcloud.ms.users.entities.Role;
import com.rwcalle.springcloud.ms.users.entities.User;
import com.rwcalle.springcloud.ms.users.repositories.RoleRepository;
import com.rwcalle.springcloud.ms.users.repositories.UserRepository;

@Service
public class UserService implements IUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;


    @Transactional(readOnly = true)
    public Optional<User> findById(Long id){
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String usernameString){
        return userRepository.findByUsername(usernameString);
    }

    @Transactional(readOnly = true)
    public Iterable<User> findAll(){
        return userRepository.findAll();
    }

    @Transactional
    public User save(User user){
        List<Role> roles = new ArrayList<>();
        Optional<Role> roleOptional = roleRepository.findByname("ROLE_USER");
        roleOptional.ifPresent(role -> roles.add(role));
        user.setRoles(roles);
        return userRepository.save(user);
    }

    @Transactional
    public void delete(Long id){
        userRepository.deleteById(id);
    }

}
