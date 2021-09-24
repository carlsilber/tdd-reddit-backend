package com.carlsilber.tddredditbackend.controllers;

import com.carlsilber.tddredditbackend.domain.Topic;
import com.carlsilber.tddredditbackend.domain.TopicVM;
import com.carlsilber.tddredditbackend.domain.User;
import com.carlsilber.tddredditbackend.services.TopicService;
import com.carlsilber.tddredditbackend.shared.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping("/users/{username}/topics")
    Page<TopicVM> getTopicsOfUser(@PathVariable String username, Pageable pageable){
        return topicService.getTopicsOfUser(username, pageable).map(TopicVM::new);
    }

    @GetMapping("/topics/{id:[0-9]+}")
    ResponseEntity<?> getTopicsRelative(@PathVariable long id, Pageable pageable, @RequestParam(name="direction", defaultValue="after") String direction) {
        if(!direction.equalsIgnoreCase("after")) {
            return ResponseEntity.ok(topicService.getOldTopics(id, pageable).map(TopicVM::new));
        }
        List<TopicVM> newTopics = topicService.getNewTopics(id, pageable).stream()
                .map(TopicVM::new).collect(Collectors.toList());
        return ResponseEntity.ok(newTopics);
    }

    @GetMapping("/users/{username}/topics/{id:[0-9]+}")
    ResponseEntity<?> getTopicsRelativeForUser(@PathVariable String username, @PathVariable long id, Pageable pageable,
                                               @RequestParam(name="direction", defaultValue="after") String direction) {
        if(!direction.equalsIgnoreCase("after")) {
            return ResponseEntity.ok(topicService.getOldTopicsOfUser(id, username, pageable).map(TopicVM::new));
        }
        List<TopicVM> newTopics = topicService.getNewTopicsOfUser(id, username, pageable).stream()
                .map(TopicVM::new).collect(Collectors.toList());
        return ResponseEntity.ok(newTopics);
    }

}
