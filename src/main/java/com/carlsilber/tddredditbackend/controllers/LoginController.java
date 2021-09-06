package com.carlsilber.tddredditbackend.controllers;

import com.carlsilber.tddredditbackend.domain.User;
import com.carlsilber.tddredditbackend.domain.UserVM;
import com.carlsilber.tddredditbackend.shared.CurrentUser;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    @PostMapping("/api/1.0/login")
    UserVM handleLogin(@CurrentUser User loggedInUser) {
        return new UserVM(loggedInUser);
    }
}