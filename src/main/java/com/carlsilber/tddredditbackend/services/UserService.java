package com.carlsilber.tddredditbackend.services;

import com.carlsilber.tddredditbackend.domain.User;
import com.carlsilber.tddredditbackend.repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}
