package com.relatosdepapel.users.controller;

import com.relatosdepapel.users.dto.CreateUserRequest;
import com.relatosdepapel.users.entity.User;
import com.relatosdepapel.users.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import com.relatosdepapel.users.dto.UserResponse;

import java.util.List;

@RestController
@RequestMapping("api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> getUsers() {
        return userService.getAllUsers();
    }

    @PostMapping
    public User createUser(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }
}