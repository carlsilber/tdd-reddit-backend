package com.carlsilber.tddredditbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class TddRedditBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TddRedditBackendApplication.class, args);
    }

}
