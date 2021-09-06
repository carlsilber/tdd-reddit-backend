package com.carlsilber.tddredditbackend;

import com.carlsilber.tddredditbackend.domain.User;
import com.carlsilber.tddredditbackend.services.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.util.stream.IntStream;

@SpringBootApplication
public class TddRedditBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TddRedditBackendApplication.class, args);
    }

    @Bean
    @Profile("!Test")
    CommandLineRunner run(UserService userService) {
        return (args) -> {
            IntStream.rangeClosed(1,15)
                    .mapToObj(i -> {
                        User user = new User();
                        user.setUsername("user"+i);
                        user.setDisplayName("display"+i);
                        user.setPassword("P4ssword");
                        return user;
                    })
                    .forEach(userService::save);
        };
    }
}
