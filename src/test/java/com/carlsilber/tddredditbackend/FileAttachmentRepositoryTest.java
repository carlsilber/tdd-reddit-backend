package com.carlsilber.tddredditbackend;

import com.carlsilber.tddredditbackend.domain.Topic;
import com.carlsilber.tddredditbackend.file.FileAttachment;
import com.carlsilber.tddredditbackend.file.FileAttachmentRepository;
import com.carlsilber.tddredditbackend.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class FileAttachmentRepositoryTest {

    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    FileAttachmentRepository fileAttachmentRepository;

    @MockBean
    private UserService userService;

    @Test
    public void findByDateBeforeAndTopicIsNull_whenAttachmentsDateOlderThanOneHour_returnsAll() {
        testEntityManager.persist(getOneHourOldFileAttachment());
        testEntityManager.persist(getOneHourOldFileAttachment());
        testEntityManager.persist(getOneHourOldFileAttachment());
        Date oneHourAgo = new Date(System.currentTimeMillis() - (60 * 60 * 1000));
        List<FileAttachment> attachments = fileAttachmentRepository.findByDateBeforeAndTopicIsNull(oneHourAgo);
        assertThat(attachments.size()).isEqualTo(3);
    }

    @Test
    public void findByDateBeforeAndTopicIsNull_whenAttachmentsDateOlderThanOneHorButHaveTopic_returnsNone() {
        Topic topic1 = testEntityManager.persist(TestUtil.createValidTopic());
        Topic topic2 = testEntityManager.persist(TestUtil.createValidTopic());
        Topic topic3 = testEntityManager.persist(TestUtil.createValidTopic());

        testEntityManager.persist(getOldFileAttachmentWithTopic(topic1));
        testEntityManager.persist(getOldFileAttachmentWithTopic(topic2));
        testEntityManager.persist(getOldFileAttachmentWithTopic(topic3));
        Date oneHourAgo = new Date(System.currentTimeMillis() - (60 * 60 * 1000));
        List<FileAttachment> attachments = fileAttachmentRepository.findByDateBeforeAndTopicIsNull(oneHourAgo);
        assertThat(attachments.size()).isEqualTo(0);
    }

    @Test
    public void findByDateBeforeAndTopicIsNull_whenAttachmentsDateWithinOneHour_returnsNone() {
        testEntityManager.persist(getFileAttachmentWithinOneHour());
        testEntityManager.persist(getFileAttachmentWithinOneHour());
        testEntityManager.persist(getFileAttachmentWithinOneHour());
        Date oneHourAgo = new Date(System.currentTimeMillis() - (60 * 60 * 1000));
        List<FileAttachment> attachments = fileAttachmentRepository.findByDateBeforeAndTopicIsNull(oneHourAgo);
        assertThat(attachments.size()).isEqualTo(0);
    }

    @Test
    public void findByDateBeforeAndTopicIsNull_whenSomeAttachmentsOldSomeNewAndSomeWithTopic_returnsAttachmentsWithOlderAndNoTopicAssigned() {
        Topic topic1 = testEntityManager.persist(TestUtil.createValidTopic());
        testEntityManager.persist(getOldFileAttachmentWithTopic(topic1));
        testEntityManager.persist(getOneHourOldFileAttachment());
        testEntityManager.persist(getFileAttachmentWithinOneHour());
        Date oneHourAgo = new Date(System.currentTimeMillis() - (60 * 60 * 1000));
        List<FileAttachment> attachments = fileAttachmentRepository.findByDateBeforeAndTopicIsNull(oneHourAgo);
        assertThat(attachments.size()).isEqualTo(1);
    }

    private FileAttachment getOneHourOldFileAttachment() {
        Date date = new Date(System.currentTimeMillis() - (60 * 60 * 1000) - 1);
        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.setDate(date);
        return fileAttachment;
    }

    private FileAttachment getFileAttachmentWithinOneHour() {
        Date date = new Date(System.currentTimeMillis() - (60 * 1000));
        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.setDate(date);
        return fileAttachment;
    }

    private FileAttachment getOldFileAttachmentWithTopic(Topic topic) {
        FileAttachment fileAttachment = getOneHourOldFileAttachment();
        fileAttachment.setTopic(topic);
        return fileAttachment;
    }

}
