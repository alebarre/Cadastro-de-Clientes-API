package com.alebarre.cadastro_clientes.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JWTAuthFilter extends OncePerRequestFilter {
    private final JWTService jwt;
    private final UserDetailsService uds;

    public JWTAuthFilter(JWTService jwt, UserDetailsService uds) {
        this.jwt = jwt; this.uds = uds;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        final String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            chain.doFilter(req, res); return;
        }

        final String token = auth.substring(7);
        String username;
        try { username = jwt.extractUsername(token); }
        catch (Exception e) { chain.doFilter(req, res); return; }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails user = uds.loadUserByUsername(username);
            if (jwt.isValid(token, user.getUsername())) {
                var authToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        chain.doFilter(req, res);
    }
}
