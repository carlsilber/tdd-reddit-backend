package com.carlsilber.tddredditbackend.controllers;

import com.carlsilber.tddredditbackend.domain.User;
import com.carlsilber.tddredditbackend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/api/1.0/users")
    void createUser(@RequestBody User user) {
        userService.save(user);
    }
}
