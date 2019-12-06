package com.carlsilber.tddredditbackend.validators;

import com.carlsilber.tddredditbackend.domain.User;
import com.carlsilber.tddredditbackend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String>{

    @Autowired
    UserRepository userRepository;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        User inDB = userRepository.findByUsername(value);
        if(inDB == null) {
            return true;
        }

        return false;
    }

}
