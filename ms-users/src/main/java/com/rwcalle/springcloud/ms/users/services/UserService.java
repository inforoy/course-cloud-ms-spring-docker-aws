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
    public Optional<User> findByUsername(String usernameString){
        return userRepository.findByUsername(usernameString);
    }

    @Transactional(readOnly = true)
    public Iterable<User> findAll(){
        return userRepository.findAll();
    }

    @Transactional
    @Override
    public User save(User user){
        user.setPasswordString(passwordEncoder.encode(user.getPasswordString()));
        user.setRoles(getRoles(user));
        return userRepository.save(user);
    }

    @Override
    public Optional<User> update(User user, Long id) {
        Optional<User> userOptional = this.findById(id);
        return userOptional.map(userDB -> {
            userDB.setEmailString(user.getEmailString());
            userDB.setUsernameString(user.getUsernameString());
            if(user.isEnabled() != null){
                userDB.setEnabled(user.isEnabled());
            }
            
            user.setRoles(getRoles(user));
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
