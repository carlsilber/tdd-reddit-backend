package com.carlsilber.tddredditbackend.repositories;

import com.carlsilber.tddredditbackend.domain.Topic;
import com.carlsilber.tddredditbackend.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface TopicRepository extends JpaRepository<Topic, Long>, JpaSpecificationExecutor {
    Page<Topic> findByUser(User user, Pageable pageable);

}
