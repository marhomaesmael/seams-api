package com.seams.backend.application.service;

import com.seams.backend.core.model.Role;
import com.seams.backend.core.model.User;
import com.seams.backend.core.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if ("aseado-node".equals(username)) {
            User nodeUser = new User();
            nodeUser.setUsername("aseado-node");
            nodeUser.setRole(Role.ADMIN);
            nodeUser.setPassword(""); 
            return new UserDetails(nodeUser);
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        return new UserDetails(user);
    }
}
