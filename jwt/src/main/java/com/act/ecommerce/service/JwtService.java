package com.act.ecommerce.service;

import com.act.ecommerce.dao.UserDao;
import com.act.ecommerce.entity.JwtRequest;
import com.act.ecommerce.entity.JwtResponse;
import com.act.ecommerce.entity.User;
import com.act.ecommerce.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JwtService implements UserDetailsService {

    private final UserDao userDao;
    private final JwtUtil jwtUtil;
    private final AuthenticationConfiguration authenticationConfiguration;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JwtService.class);

    public JwtService(UserDao userDao, JwtUtil jwtUtil, AuthenticationConfiguration authenticationConfiguration) {
        this.userDao = userDao;
        this.jwtUtil = jwtUtil;
        this.authenticationConfiguration = authenticationConfiguration;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userDao.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUserName(),
                user.getUserPassword(),
                getAuthorities(user)
        );
    }

    private Set<SimpleGrantedAuthority> getAuthorities(User user) {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getRole_name()))
                .collect(Collectors.toSet());
    }

    public JwtResponse createToken(JwtRequest jwtRequest) throws Exception {
        String username = jwtRequest.getUserName();
        String password = jwtRequest.getUserPassword();
        logger.info("Creating JWT token for user: {}", username);
        authenticate(username, password);
        UserDetails userDetails = loadUserByUsername(username);

        String token = jwtUtil.generateToken(userDetails.getUsername());
        User user = userDao.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return new JwtResponse(user, token);
    }

    private void authenticate(String username, String password) throws Exception {
        AuthenticationManager authenticationManager = authenticationConfiguration.getAuthenticationManager();
        try {
            logger.info("Authenticating user: {}", username,password);
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new RuntimeException("User is disabled");
        } catch (UsernameNotFoundException e) {
            throw new RuntimeException("User not found");
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid credentials");
        } catch (Exception e) {
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }
}
