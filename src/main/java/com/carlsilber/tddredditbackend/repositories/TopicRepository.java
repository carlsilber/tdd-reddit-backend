package com.carlsilber.tddredditbackend.repositories;

import com.carlsilber.tddredditbackend.domain.Topic;
import com.carlsilber.tddredditbackend.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    Page<Topic> findByUser(User user, Pageable pageable);

    Page<Topic> findByIdLessThan(long id, Pageable pageable);

    Page<Topic> findByIdLessThanAndUser(long id, User user, Pageable pageable);

}
