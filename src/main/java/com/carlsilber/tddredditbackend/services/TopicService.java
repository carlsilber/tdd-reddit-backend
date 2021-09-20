package com.carlsilber.tddredditbackend.services;

import com.carlsilber.tddredditbackend.domain.Topic;
import com.carlsilber.tddredditbackend.repositories.TopicRepository;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TopicService {

    TopicRepository topicRepository;

    public TopicService(TopicRepository topicRepository) {
        super();
        this.topicRepository = topicRepository;
    }

    public void save(Topic topic) {
        topic.setTimestamp(new Date());
        topicRepository.save(topic);
    }

}
