package com.carlsilber.tddredditbackend.repositories;

import com.carlsilber.tddredditbackend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
