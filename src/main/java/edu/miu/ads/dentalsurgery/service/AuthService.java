package edu.miu.ads.dentalsurgery.service;

import edu.miu.ads.dentalsurgery.domain.AppUser;
import edu.miu.ads.dentalsurgery.dto.AuthResponse;
import edu.miu.ads.dentalsurgery.dto.LoginRequest;
import edu.miu.ads.dentalsurgery.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        AppUser user = (AppUser) authentication.getPrincipal();
        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                "Bearer",
                user.getUsername(),
                user.getRole().name(),
                jwtService.getJwtExpirationMs());
    }
}
