package com.carlsilber.tddredditbackend.controllers;

import com.carlsilber.tddredditbackend.domain.Topic;
import com.carlsilber.tddredditbackend.domain.User;
import com.carlsilber.tddredditbackend.services.TopicService;
import com.carlsilber.tddredditbackend.shared.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/1.0")
public class TopicController {

    @Autowired
    TopicService topicService;

    @PostMapping("/topics")
    void createTopic(@Valid @RequestBody Topic topic, @CurrentUser User user) {
        topicService.save(user, topic);
    }

}
