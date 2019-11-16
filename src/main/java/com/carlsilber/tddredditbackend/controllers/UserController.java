package com.carlsilber.tddredditbackend.controllers;

import com.carlsilber.tddredditbackend.domain.User;
import com.carlsilber.tddredditbackend.services.UserService;
import com.carlsilber.tddredditbackend.shared.GenericResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/api/1.0/users")
    GenericResponse createUser(@RequestBody User user) {
        userService.save(user);
        return new GenericResponse("User saved");
    }
}
