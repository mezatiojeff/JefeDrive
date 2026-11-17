package com.jefedrive.backend.controller;

import com.jefedrive.backend.entity.User;
import com.jefedrive.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Public client registration
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public User registerClient(@RequestBody User user) {
        return userService.registerClient(user);
    }

    // Staff creation - must be protected by ADMIN authorization
    @PostMapping("/staff")
    @ResponseStatus(HttpStatus.CREATED)
    public User createStaff(
            @RequestBody User user,
            @RequestParam User.Role role) {
        return userService.createStaff(user, role);
    }
}