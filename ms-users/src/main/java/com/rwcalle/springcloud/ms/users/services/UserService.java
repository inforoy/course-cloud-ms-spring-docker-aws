package com.rwcalle.springcloud.ms.users.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    @Override
    public Optional<User> findById(Long id){
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<User> findByUsername(String username){
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    @Override
    public Iterable<User> findAll(){
        return userRepository.findAll();
    }

    @Transactional
    @Override
    public User save(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(getRoles(user));
        user.setEnabled(true);
        return userRepository.save(user);
    }

    @Override
    public Optional<User> update(User user, Long id) {
        Optional<User> userOptional = this.findById(id);
        return userOptional.map(userDB -> {
            userDB.setEmail(user.getEmail());
            userDB.setUsername(user.getUsername());
            if(user.isEnabled() == null){
                userDB.setEnabled(true);
            } else {
                userDB.setEnabled(user.isEnabled());
            }
            
            userDB.setRoles(getRoles(user));
            return Optional.of(userRepository.save(userDB));
        }).orElseGet(() -> Optional.empty());
    }

    @Transactional
    @Override
    public void delete(Long id){
        userRepository.deleteById(id);
    }

    private List<Role> getRoles(User user) {
        List<Role> roles = new ArrayList<>();
        Optional<Role> roleOptional = roleRepository.findByname("ROLE_USER");
        roleOptional.ifPresent(roles::add);

        if(user.isAdmin()){
            Optional<Role> adminRoleOptional = roleRepository.findByname("ROLE_ADMIN");
            adminRoleOptional.ifPresent(roles::add);
        }
        
        return roles;
    }

}
