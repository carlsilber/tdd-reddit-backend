package com.carlsilber.tddredditbackend.repositories;

import com.carlsilber.tddredditbackend.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    Page<User> findByUsernameNot(String username, Pageable page);

}
