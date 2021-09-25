package com.carlsilber.tddredditbackend.services;

import com.carlsilber.tddredditbackend.domain.Topic;
import com.carlsilber.tddredditbackend.domain.User;
import com.carlsilber.tddredditbackend.repositories.TopicRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
        Specification<Topic> spec = Specification.where(idLessThan(id));
        if (username != null) {
            User inDB = userService.getByUsername(username);
            spec = spec.and(userIs(inDB));
        }
        return topicRepository.findAll(spec, pageable);
    }

    public List<Topic> getNewTopics(long id, String username, Pageable pageable) {
        Specification<Topic> spec = Specification.where(idGreaterThan(id));
        if (username != null) {
            User inDB = userService.getByUsername(username);
            spec = spec.and(userIs(inDB));
        }
        return topicRepository.findAll(spec, pageable.getSort());
    }

    public long getNewTopicsCount(long id, String username) {
        Specification<Topic> spec = Specification.where(idGreaterThan(id));
        if (username != null) {
            User inDB = userService.getByUsername(username);
            spec = spec.and(userIs(inDB));
        }
        return topicRepository.count(spec);
    }

    private Specification<Topic> userIs(User user) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user"), user);
    }

    private Specification<Topic> idLessThan(long id) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get("id"), id);
    }

    private Specification<Topic> idGreaterThan(long id) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get("id"), id);
    }

}