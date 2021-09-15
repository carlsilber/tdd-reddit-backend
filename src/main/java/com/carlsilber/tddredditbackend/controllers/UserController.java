package com.carlsilber.tddredditbackend.controllers;

import com.carlsilber.tddredditbackend.domain.User;
import com.carlsilber.tddredditbackend.domain.UserUpdateVM;
import com.carlsilber.tddredditbackend.domain.UserVM;
import com.carlsilber.tddredditbackend.error.ApiError;
import com.carlsilber.tddredditbackend.services.UserService;
import com.carlsilber.tddredditbackend.shared.CurrentUser;
import com.carlsilber.tddredditbackend.shared.GenericResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/1.0")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/users")
    GenericResponse createUser(@Valid @RequestBody User user) {
        userService.save(user);
        return new GenericResponse("User saved");
    }

    @GetMapping("/users")
    Page<UserVM> getUsers(@CurrentUser User loggedInUser, Pageable page) {
        return userService.getUsers(loggedInUser, page).map(UserVM::new);
    }

    @GetMapping("/users/{username}")
    UserVM getUserByName(@PathVariable String username) {
        User user = userService.getByUsername(username);
        return new UserVM(user);
    }
    @PutMapping("/users/{id:[0-9]+}")
    @PreAuthorize("#id == principal.id")
    UserVM updateUser(@PathVariable long id, @Valid @RequestBody(required = false) UserUpdateVM userUpdate) {
        User updated = userService.update(id, userUpdate);
        return new UserVM(updated);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiError handleValidationException(MethodArgumentNotValidException exception, HttpServletRequest request) {
        ApiError apiError = new ApiError(400, "Validation error", request.getServletPath());

        BindingResult result = exception.getBindingResult();

        Map<String, String> validationErrors = new HashMap<>();

        for(FieldError fieldError: result.getFieldErrors()) {
            validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        apiError.setValidationErrors(validationErrors);

        return apiError;
    }
}
