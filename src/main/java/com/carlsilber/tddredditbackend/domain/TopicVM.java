package com.carlsilber.tddredditbackend.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data@NoArgsConstructor
public class TopicVM {

    private long id;

    private String content;

    private long date;

    private UserVM user;

    public TopicVM(Topic topic) {
        this.setId(topic.getId());
        this.setContent(topic.getContent());
        this.setDate(topic.getTimestamp().getTime());
        this.setUser(new UserVM(topic.getUser()));
    }

}
