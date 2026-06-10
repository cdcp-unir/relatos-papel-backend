package com.relatosdepapel.users.service;

import com.relatosdepapel.users.dto.*;
import com.relatosdepapel.users.entity.User;
import com.relatosdepapel.users.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RedisService redisService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, RedisService redisService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.redisService = redisService;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getRole()
                ))
                .toList();
    }

    public User createUser(CreateUserRequest user) {
        User newUser = new User();
         newUser.setFirstName(user.getFirstName());
         newUser.setLastName(user.getLastName());
         newUser.setEmail(user.getEmail());
         newUser.setPassword(passwordEncoder.encode(user.getPassword()));
         newUser.setRole(user.getRole());

         return userRepository.save(newUser);
    }

    public TokenResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        boolean validPassword = passwordEncoder.matches( request.getPassword(),user.getPassword());

        if (!validPassword) {
            throw new RuntimeException("Credenciales inválidas");
        }

        String jwt = jwtService.generateToken(user.getId(), user.getEmail(),  user.getRole());
        String opaqueToken = UUID.randomUUID().toString();
        redisService.saveToken(opaqueToken, jwt);

        return new TokenResponse(opaqueToken);
    }

    public ValidateTokenResponse validateToken(String opaqueToken) {
        String jwt = redisService.getJwt(opaqueToken);

        if (jwt == null) {
            return new ValidateTokenResponse(null, false, null, null, null);
        }

        Claims claims = jwtService.obtainClaims(jwt);

        Integer userId = Integer.valueOf(claims.getSubject());
        String email = claims.get("email", String.class);
        String role = claims.get("role", String.class);

        return new ValidateTokenResponse(
                jwt,
                true,
                userId,
                email,
                role
        );
    }

    public TokenResponse refreshToken(String opaqueToken) {
        String jwt = redisService.getJwt(opaqueToken);

        if(jwt == null) {
            throw new RuntimeException("Token invalido");
        }

        Claims claims = jwtService.obtainClaims(jwt);

        Integer id = Integer.valueOf(claims.getSubject());

        String email = claims.get("email", String.class);

        String role = claims.get("role", String.class);

        String newJwt = jwtService.generateToken( id, email, role);

        String newOpaqueToken = UUID.randomUUID().toString();

        redisService.saveToken(newOpaqueToken, newJwt);

        redisService.deleteToken(opaqueToken);

        return new TokenResponse(newOpaqueToken);
    }
}