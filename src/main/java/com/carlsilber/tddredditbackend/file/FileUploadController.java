package com.carlsilber.tddredditbackend.file;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/1.0")
public class FileUploadController {

    @PostMapping("/topics/upload")
    void uploadForTopic() {

    }

}
