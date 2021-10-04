package com.carlsilber.tddredditbackend.domain;

import com.carlsilber.tddredditbackend.file.FileAttachment;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@Entity
public class Topic {

    @Id
    @GeneratedValue
    private long id;

    @NotNull
    @Size(min = 10, max=5000)
    @Column(length = 5000)
    private String content;

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @ManyToOne
    private User user;

    @OneToOne(mappedBy = "topic", orphanRemoval = true)
    private FileAttachment attachment;
}
