package com.carlsilber.tddredditbackend.services;

import com.carlsilber.tddredditbackend.domain.Topic;
import com.carlsilber.tddredditbackend.domain.User;
import com.carlsilber.tddredditbackend.repositories.TopicRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TopicSecurityService {

    TopicRepository topicRepository;

    public TopicSecurityService(TopicRepository topicRepository) {
        super();
        this.topicRepository = topicRepository;
    }

    public boolean isAllowedToDelete(long topicId, User loggedInUser) {
        Optional<Topic> optionalTopic = topicRepository.findById(topicId);
        if (optionalTopic.isPresent()) {
            Topic inDB = optionalTopic.get();
            return inDB.getUser().getId() == loggedInUser.getId();
        }
        return false;
    }

}
