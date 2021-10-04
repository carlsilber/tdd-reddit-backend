package com.carlsilber.tddredditbackend.controllers;

import com.carlsilber.tddredditbackend.domain.Topic;
import com.carlsilber.tddredditbackend.domain.TopicVM;
import com.carlsilber.tddredditbackend.domain.User;
import com.carlsilber.tddredditbackend.services.TopicService;
import com.carlsilber.tddredditbackend.shared.CurrentUser;
import com.carlsilber.tddredditbackend.shared.GenericResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
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

    @GetMapping({"/topics/{id:[0-9]+}", "/users/{username}/topics/{id:[0-9]+}"})
    ResponseEntity<?> getTopicsRelative(@PathVariable long id,
                                        @PathVariable(required = false) String username,
                                        Pageable pageable,
                                        @RequestParam(name="direction", defaultValue="after") String direction,
                                        @RequestParam(name="count", defaultValue="false", required=false) boolean count) {
        if(!direction.equalsIgnoreCase("after")) {
            return ResponseEntity.ok(topicService.getOldTopics(id, username, pageable).map(TopicVM::new));
        }
        if(count) {
            long newTopicCount = topicService.getNewTopicsCount(id, username);
            return ResponseEntity.ok(Collections.singletonMap("count", newTopicCount));
        }

        List<TopicVM> newTopics = topicService.getNewTopics(id, username, pageable).stream()
                .map(TopicVM::new).collect(Collectors.toList());
        return ResponseEntity.ok(newTopics);
    }

    @DeleteMapping("/topics/{id:[0-9]+}")
    GenericResponse deleteTopic(@PathVariable long id) {
        topicService.deleteTopic(id);
        return new GenericResponse("Topic is removed");
    }

}
