package com.carlsilber.tddredditbackend.controllers;

import com.carlsilber.tddredditbackend.domain.Topic;
import com.carlsilber.tddredditbackend.domain.TopicVM;
import com.carlsilber.tddredditbackend.domain.User;
import com.carlsilber.tddredditbackend.services.TopicService;
import com.carlsilber.tddredditbackend.shared.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/1.0")
public class TopicController {

    @Autowired
    TopicService topicService;

    @PostMapping("/topics")
    TopicVM createTopic(@Valid @RequestBody Topic topic, @CurrentUser User user) {
        return new TopicVM(topicService.save(user, topic));
    }

    @GetMapping("/topics")
    Page<TopicVM> getAllTopics(Pageable pageable) {
        return topicService.getAllTopics(pageable).map(TopicVM::new);
    }

}
