package com.carlsilber.tddredditbackend.repositories;

import com.carlsilber.tddredditbackend.domain.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic, Long> {

}
