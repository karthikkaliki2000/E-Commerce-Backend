package com.act.ecommerce.configuration;

import com.act.ecommerce.service.JwtService;
import com.act.ecommerce.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    public static String CURRENT_USER = "";
    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    @Lazy
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");
        String jwt = null;
        String username = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);

            try {
                username = jwtUtil.getUserNameFromToken(jwt);
                this.CURRENT_USER = username;

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = jwtService.loadUserByUsername(username);

                    if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
                        UsernamePasswordAuthenticationToken authenticationToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authenticationToken.setDetails(
                                new org.springframework.security.web.authentication.WebAuthenticationDetailsSource()
                                        .buildDetails(request)
                        );
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                        logger.info("JWT Token validated successfully for user: {}", username);
                    } else {
                        logger.warn("Invalid JWT Token for user: {}", username);
                    }
                }
            } catch (IllegalArgumentException e) {
                logger.error("JWT parsing error: {}", e.getMessage());
            } catch (ExpiredJwtException e) {
                logger.warn("Expired JWT token for user: {}", username);
            } catch (JwtException e) {
                logger.error("JWT validation failed: {}", e.getMessage());
            } catch (Exception e) {
                logger.error("Unexpected error during JWT validation: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}