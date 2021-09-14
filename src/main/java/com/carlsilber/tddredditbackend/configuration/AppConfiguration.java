package com.carlsilber.tddredditbackend.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "redd")
@Data
public class AppConfiguration {

    String uploadPath;

    String profileImagesFolder = "profile";

    String attachmentsFolder = "attachments";

    public String getFullProfileImagesPath() {
        return this.uploadPath + "/" + this.profileImagesFolder;
    }

    public String getFullAttachmentsPath() {
        return this.uploadPath + "/" + this.attachmentsFolder;
    }
}
