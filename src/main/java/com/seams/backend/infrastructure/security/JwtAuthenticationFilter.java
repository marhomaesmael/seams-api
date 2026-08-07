package com.seams.backend.infrastructure.security;

import com.seams.backend.core.repository.ConnectedNodeRepository;
import com.seams.backend.application.service.UserDetailService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailService userDetailService;
    private final ConnectedNodeRepository connectedNodeRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailService userDetailService, ConnectedNodeRepository connectedNodeRepository) {
        this.jwtService = jwtService;
        this.userDetailService = userDetailService;
        this.connectedNodeRepository = connectedNodeRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        try {
            username = jwtService.extractUsername(jwt);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailService.loadUserByUsername(username);
                
                // Node verification logic
                if ("aseado-node".equals(username)) {
                    boolean isActiveNode = connectedNodeRepository.findByToken(jwt)
                            .map(node -> "ACTIVE".equals(node.getStatus()))
                            .orElse(false);
                    
                    if (!isActiveNode) {
                        filterChain.doFilter(request, response);
                        return;
                    }
                }

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Log error if needed, but continue chain so Spring Security can handle auth failure
        }
        
        filterChain.doFilter(request, response);
    }
}
