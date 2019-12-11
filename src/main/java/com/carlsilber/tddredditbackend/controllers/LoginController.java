package com.carlsilber.tddredditbackend.controllers;

import com.carlsilber.tddredditbackend.domain.User;
import com.carlsilber.tddredditbackend.shared.CurrentUser;
import com.carlsilber.tddredditbackend.views.Views;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    @PostMapping("/api/1.0/login")
    @JsonView(Views.Base.class)
    User handleLogin(@CurrentUser User loggedInUser) {
        return loggedInUser;
    }

}