package com.nazor.practicejava.user;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads users for Spring Security's {@code DaoAuthenticationProvider}, keyed by
 * email and ignoring soft-deleted accounts.
 */
@Service
public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public JpaUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmailAndDeletedAtIsNull(username)
                .orElseThrow(() -> new UsernameNotFoundException("No active user with email: " + username));
    }
}
