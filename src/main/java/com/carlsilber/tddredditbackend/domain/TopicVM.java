package com.carlsilber.tddredditbackend.domain;

import com.carlsilber.tddredditbackend.file.FileAttachmentVM;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data@NoArgsConstructor
public class TopicVM {

    private long id;

    private String content;

    private long date;

    private UserVM user;

    private FileAttachmentVM attachment;

    public TopicVM(Topic topic) {
        this.setId(topic.getId());
        this.setContent(topic.getContent());
        this.setDate(topic.getTimestamp().getTime());
        this.setUser(new UserVM(topic.getUser()));
        if(topic.getAttachment() != null){
            this.setAttachment(new FileAttachmentVM(topic.getAttachment()));
        }
    }

}
