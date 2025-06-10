package com.example.userbankingservice.security;

import com.example.userbankingservice.entity.User;
import com.example.userbankingservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
        log.info("User found with id: " + userId);
        return org.springframework.security.core.userdetails.User
                .withUsername(userId)
                .password(user.getPassword())
                .roles("USER")
                .build();
    }
}