package com.example.berijalanassesment.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring("Bearer ".length()).trim();
        if (token.isEmpty()) {
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }
        Jwt jwt;
        try {
            jwt = jwtService.decodeAccessToken(token);
        } catch (RuntimeException ex) {
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            jwt.getSubject(),
            null,
            authorities
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<String> permissions = jwt.getClaimAsStringList("permissions");
        if (permissions == null) {
            return List.of();
        }

        return permissions.stream()
            .filter(value -> value != null && !value.isBlank())
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toSet());
    }
}
