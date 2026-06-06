package com.relatosdepapel.users.controller;

import com.relatosdepapel.users.dto.*;
import com.relatosdepapel.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/token")
    public TokenResponse createToken(@RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @PostMapping("/validate")
    public ValidateTokenResponse validateToken(@RequestBody ValidateTokenRequest request) {
        return userService.validateToken(request.getToken());
    }

    @PostMapping("/refresh")
    public TokenResponse refreshToken(@RequestBody RefreshTokenRequest request) {
     return userService.refreshToken(request.getToken());
    }
}