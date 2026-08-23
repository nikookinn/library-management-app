package com.nikookinn.librarymanagement.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikookinn.librarymanagement.exception.ErrorResponse;
import com.nikookinn.librarymanagement.service.CustomUserDetailsService;
import com.nikookinn.librarymanagement.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authorizationHeader.substring(7);
            String email = jwtService.getEmailFromToken(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredJwtException e) {
            SecurityContextHolder.clearContext();
            log.warn("Rejected request with expired JWT for path {}", request.getRequestURI());
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Token has expired");
            return;
        } catch (MalformedJwtException e) {
            SecurityContextHolder.clearContext();
            log.warn("Rejected request with malformed JWT for path {}", request.getRequestURI());
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Token is malformed");
            return;
        } catch (JwtException | IllegalArgumentException e) {
            SecurityContextHolder.clearContext();
            log.warn("Rejected request with invalid JWT for path {}", request.getRequestURI());
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid token");
            return;
        } catch (UsernameNotFoundException e) {
            SecurityContextHolder.clearContext();
            log.warn("Rejected request with JWT for unknown user, path {}", request.getRequestURI());
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "User not found");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse error = new ErrorResponse(
                status.value(),
                message,
                "Unauthorized"
        );
        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
