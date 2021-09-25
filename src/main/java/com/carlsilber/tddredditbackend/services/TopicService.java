package com.carlsilber.tddredditbackend.services;

import com.carlsilber.tddredditbackend.domain.Topic;
import com.carlsilber.tddredditbackend.domain.User;
import com.carlsilber.tddredditbackend.repositories.TopicRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class TopicService {

    TopicRepository topicRepository;

    UserService userService;

    public TopicService(TopicRepository topicRepository, UserService userService) {
        super();
        this.topicRepository = topicRepository;
        this.userService = userService;
    }

    public Topic save(User user, Topic topic) {
        topic.setTimestamp(new Date());
        topic.setUser(user);
        return topicRepository.save(topic);
    }

    public Page<Topic> getAllTopics(Pageable pageable) {
        return topicRepository.findAll(pageable);
    }

    public Page<Topic> getTopicsOfUser(String username, Pageable pageable) {
        User inDB = userService.getByUsername(username);
        return topicRepository.findByUser(inDB, pageable);
    }

    public Page<Topic> getOldTopics(long id, String username, Pageable pageable) {
        if (username == null) {
            return topicRepository.findByIdLessThan(id, pageable);
        }
        User inDB = userService.getByUsername(username);
        return topicRepository.findByIdLessThanAndUser(id, inDB, pageable);
    }

    public List<Topic> getNewTopics(long id, String username, Pageable pageable) {
        if (username == null) {
            return topicRepository.findByIdGreaterThan(id, pageable.getSort());
        }
        User inDB = userService.getByUsername(username);
        return topicRepository.findByIdGreaterThanAndUser(id, inDB, pageable.getSort());
    }

    public long getNewTopicsCount(long id, String username) {
        if (username == null) {
        return topicRepository.countByIdGreaterThan(id);
        }
        User inDB = userService.getByUsername(username);
        return topicRepository.countByIdGreaterThanAndUser(id, inDB);
    }

}
