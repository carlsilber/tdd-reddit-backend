package com.carlsilber.tddredditbackend;

import com.carlsilber.tddredditbackend.configuration.AppConfiguration;
import com.carlsilber.tddredditbackend.domain.Topic;
import com.carlsilber.tddredditbackend.domain.TopicVM;
import com.carlsilber.tddredditbackend.domain.User;
import com.carlsilber.tddredditbackend.error.ApiError;
import com.carlsilber.tddredditbackend.file.FileAttachment;
import com.carlsilber.tddredditbackend.file.FileAttachmentRepository;
import com.carlsilber.tddredditbackend.file.FileService;
import com.carlsilber.tddredditbackend.repositories.TopicRepository;
import com.carlsilber.tddredditbackend.repositories.UserRepository;
import com.carlsilber.tddredditbackend.services.TopicService;
import com.carlsilber.tddredditbackend.services.UserService;
import com.carlsilber.tddredditbackend.shared.GenericResponse;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TopicControllerTest {

    private static final String API_1_0_TOPICS = "/api/1.0/topics";

    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TopicRepository topicRepository;

    @Autowired
    TopicService topicService;

    @Autowired
    FileAttachmentRepository fileAttachmentRepository;

    @Autowired
    FileService fileService;

    @Autowired
    AppConfiguration appConfiguration;

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    @Before
    public void cleanup() throws IOException {
        fileAttachmentRepository.deleteAll();
        topicRepository.deleteAll();
        userRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
        FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));
    }

    @Test
    public void postTopic_whenTopicIsValidAndUserIsAuthorized_receiveOk() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Topic topic = TestUtil.createValidTopic();
        ResponseEntity<Object> response = postTopic(topic, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    @Test
    public void postTopic_whenTopicIsValidAndUserIsUnauthorized_receiveUnauthorized() {
        Topic topic = TestUtil.createValidTopic();
        ResponseEntity<Object> response = postTopic(topic, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void postTopic_whenTopicIsValidAndUserIsUnauthorized_receiveApiError() {
        Topic topic = TestUtil.createValidTopic();
        ResponseEntity<ApiError> response = postTopic(topic, ApiError.class);
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }


    @Test
    public void postTopic_whenTopicIsValidAndUserIsAuthorized_topicSavedToDatabase() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Topic topic = TestUtil.createValidTopic();
        postTopic(topic, Object.class);

        assertThat(topicRepository.count()).isEqualTo(1);
    }

    @Test
    public void postTopic_whenTopicIsValidAndUserIsAuthorized_topicSavedToDatabaseWithTimestamp() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Topic topic = TestUtil.createValidTopic();
        postTopic(topic, Object.class);

        Topic inDB = topicRepository.findAll().get(0);

        assertThat(inDB.getTimestamp()).isNotNull();
    }

    @Test
    public void postTopic_whenTopicContentNullAndUserIsAuthorized_receiveBadRequest() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Topic topic = new Topic();
        ResponseEntity<Object> response = postTopic(topic, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postTopic_whenTopicContentLessThan10CharactersAndUserIsAuthorized_receiveBadRequest() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Topic topic = new Topic();
        topic.setContent("123456789");
        ResponseEntity<Object> response = postTopic(topic, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postTopic_whenTopicContentIs5000CharactersAndUserIsAuthorized_receiveOk() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Topic topic = new Topic();
        String veryLongString = IntStream.rangeClosed(1, 5000).mapToObj(i -> "x").collect(Collectors.joining());
        topic.setContent(veryLongString);
        ResponseEntity<Object> response = postTopic(topic, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void postTopic_whenTopicContentMoreThan5000CharactersAndUserIsAuthorized_receiveBadRequest() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Topic topic = new Topic();
        String veryLongString = IntStream.rangeClosed(1, 5001).mapToObj(i -> "x").collect(Collectors.joining());
        topic.setContent(veryLongString);
        ResponseEntity<Object> response = postTopic(topic, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }


    @Test
    public void postTopic_whenTopicContentNullAndUserIsAuthorized_receiveApiErrorWithValidationErrors() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Topic topic = new Topic();
        ResponseEntity<ApiError> response = postTopic(topic, ApiError.class);
        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertThat(validationErrors.get("content")).isNotNull();
    }

    @Test
    public void postTopic_whenTopicIsValidAndUserIsAuthorized_topicSavedWithAuthenticatedUserInfo() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Topic topic = TestUtil.createValidTopic();
        postTopic(topic, Object.class);

        Topic inDB = topicRepository.findAll().get(0);

        assertThat(inDB.getUser().getUsername()).isEqualTo("user1");
    }

    @Test
    public void postTopic_whenTopicIsValidAndUserIsAuthorized_topicCanBeAccessedFromUserEntity() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Topic topic = TestUtil.createValidTopic();
        postTopic(topic, Object.class);

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        User inDBUser = entityManager.find(User.class, user.getId());
        assertThat(inDBUser.getTopics().size()).isEqualTo(1);

    }

    @Test
    public void postTopic_whenTopicIsValidAndUserIsAuthorized_receiveTopicVM() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Topic topic = TestUtil.createValidTopic();
        ResponseEntity<TopicVM> response = postTopic(topic, TopicVM.class);
        assertThat(response.getBody().getUser().getUsername()).isEqualTo("user1");
    }


    @Test
    public void postTopic_whenTopicHasFileAttachmentAndUserIsAuthorized_fileAttachmentTopicRelationIsUpdatedInDatabase() throws IOException {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");

        MultipartFile file = createFile();

        FileAttachment savedFile = fileService.saveAttachment(file);

        Topic topic = TestUtil.createValidTopic();
        topic.setAttachment(savedFile);
        ResponseEntity<TopicVM> response = postTopic(topic, TopicVM.class);

        FileAttachment inDB = fileAttachmentRepository.findAll().get(0);
        assertThat(inDB.getTopic().getId()).isEqualTo(response.getBody().getId());
    }

    @Test
    public void postTopic_whenTopicHasFileAttachmentAndUserIsAuthorized_topicFileAttachmentRelationIsUpdatedInDatabase() throws IOException {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");

        MultipartFile file = createFile();

        FileAttachment savedFile = fileService.saveAttachment(file);

        Topic topic = TestUtil.createValidTopic();
        topic.setAttachment(savedFile);
        ResponseEntity<TopicVM> response = postTopic(topic, TopicVM.class);

        Topic inDB = topicRepository.findById(response.getBody().getId()).get();
        assertThat(inDB.getAttachment().getId()).isEqualTo(savedFile.getId());
    }

    @Test
    public void postTopic_whenTopicHasFileAttachmentAndUserIsAuthorized_receiveTopicVMWithAttachment() throws IOException {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");

        MultipartFile file = createFile();

        FileAttachment savedFile = fileService.saveAttachment(file);

        Topic topic = TestUtil.createValidTopic();
        topic.setAttachment(savedFile);
        ResponseEntity<TopicVM> response = postTopic(topic, TopicVM.class);

        assertThat(response.getBody().getAttachment().getName()).isEqualTo(savedFile.getName());
    }

    private MultipartFile createFile() throws IOException {
        ClassPathResource imageResource = new ClassPathResource("profile.png");
        byte[] fileAsByte = FileUtils.readFileToByteArray(imageResource.getFile());

        MultipartFile file = new MockMultipartFile("profile.png", fileAsByte);
        return file;
    }

    @Test
    public void getTopics_whenThereAreNoTopics_receiveOk() {
        ResponseEntity<Object> response = getTopics(new ParameterizedTypeReference<Object>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getTopics_whenThereAreNoTopics_receivePageWithZeroItems() {
        ResponseEntity<TestPage<Object>> response = getTopics(new ParameterizedTypeReference<TestPage<Object>>() {});
        assertThat(response.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    public void getTopics_whenThereAreTopics_receivePageWithItems() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());

        ResponseEntity<TestPage<Object>> response = getTopics(new ParameterizedTypeReference<TestPage<Object>>() {});
        assertThat(response.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    public void getTopics_whenThereAreTopics_receivePageWithTopicVM() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        topicService.save(user, TestUtil.createValidTopic());

        ResponseEntity<TestPage<TopicVM>> response = getTopics(new ParameterizedTypeReference<TestPage<TopicVM>>() {});
        TopicVM storedTopic = response.getBody().getContent().get(0);
        assertThat(storedTopic.getUser().getUsername()).isEqualTo("user1");
    }

    @Test
    public void getTopicsOfUser_whenUserExists_receiveOk() {
        userService.save(TestUtil.createValidUser("user1"));
        ResponseEntity<Object> response = getTopicsOfUser("user1", new ParameterizedTypeReference<Object>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getTopicsOfUser_whenUserDoesNotExist_receiveNotFound() {
        ResponseEntity<Object> response = getTopicsOfUser("unknown-user", new ParameterizedTypeReference<Object>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
    @Test
    public void getTopicsOfUser_whenUserExists_receivePageWithZeroTopics() {
        userService.save(TestUtil.createValidUser("user1"));
        ResponseEntity<TestPage<Object>> response = getTopicsOfUser("user1", new ParameterizedTypeReference<TestPage<Object>>() {});
        assertThat(response.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    public void getTopicsOfUser_whenUserExistWithTopic_receivePageWithTopicVM() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        topicService.save(user, TestUtil.createValidTopic());

        ResponseEntity<TestPage<TopicVM>> response = getTopicsOfUser("user1", new ParameterizedTypeReference<TestPage<TopicVM>>() {});
        TopicVM storedTopic = response.getBody().getContent().get(0);
        assertThat(storedTopic.getUser().getUsername()).isEqualTo("user1");
    }

    @Test
    public void getTopicsOfUser_whenUserExistWithMultipleTopics_receivePageWithMatchingTopicsCount() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());

        ResponseEntity<TestPage<TopicVM>> response = getTopicsOfUser("user1", new ParameterizedTypeReference<TestPage<TopicVM>>() {});
        assertThat(response.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    public void getTopicsOfUser_whenMultipleUserExistWithMultipleTopics_receivePageWithMatchingTopicsCount() {
        User userWithThreeTopics = userService.save(TestUtil.createValidUser("user1"));
        IntStream.rangeClosed(1, 3).forEach(i -> {
            topicService.save(userWithThreeTopics, TestUtil.createValidTopic());
        });

        User userWithFiveTopics = userService.save(TestUtil.createValidUser("user2"));
        IntStream.rangeClosed(1, 5).forEach(i -> {
            topicService.save(userWithFiveTopics, TestUtil.createValidTopic());
        });

        ResponseEntity<TestPage<TopicVM>> response = getTopicsOfUser(userWithFiveTopics.getUsername(), new ParameterizedTypeReference<TestPage<TopicVM>>() {});
        assertThat(response.getBody().getTotalElements()).isEqualTo(5);
    }

    @Test
    public void getOldTopics_whenThereAreNoTopics_receiveOk() {
        ResponseEntity<Object> response = getOldTopics(5, new ParameterizedTypeReference<Object>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getOldTopics_whenThereAreTopics_receivePageWithItemsBeforeProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());
        Topic fourth = topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());

        ResponseEntity<TestPage<Object>> response = getOldTopics(fourth.getId(), new ParameterizedTypeReference<TestPage<Object>>() {});
        assertThat(response.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    public void getOldTopics_whenThereAreTopics_receivePageWithTopicVMBeforeProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());
        Topic fourth = topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());

        ResponseEntity<TestPage<TopicVM>> response = getOldTopics(fourth.getId(), new ParameterizedTypeReference<TestPage<TopicVM>>() {});
        assertThat(response.getBody().getContent().get(0).getDate()).isGreaterThan(0);
    }

    @Test
    public void getOldTopicsOfUser_whenUserExistThereAreNoTopics_receiveOk() {
        userService.save(TestUtil.createValidUser("user1"));
        ResponseEntity<Object> response = getOldTopicsOfUser(5, "user1", new ParameterizedTypeReference<Object>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
    @Test
    public void getOldTopicsOfUser_whenUserExistAndThereAreTopics_receivePageWithItemsBeforeProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());
        Topic fourth = topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());

        ResponseEntity<TestPage<Object>> response = getOldTopicsOfUser(fourth.getId(), "user1", new ParameterizedTypeReference<TestPage<Object>>() {});
        assertThat(response.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    public void getOldTopicsOfUser_whenUserExistAndThereAreTopics_receivePageWithTopicVMBeforeProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());
        Topic fourth = topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());

        ResponseEntity<TestPage<TopicVM>> response = getOldTopicsOfUser(fourth.getId(), "user1", new ParameterizedTypeReference<TestPage<TopicVM>>() {});
        assertThat(response.getBody().getContent().get(0).getDate()).isGreaterThan(0);
    }

    @Test
    public void getOldTopicsOfUser_whenUserDoesNotExistThereAreNoTopics_receiveNotFound() {
        ResponseEntity<Object> response = getOldTopicsOfUser(5, "user1", new ParameterizedTypeReference<Object>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getOldTopicsOfUser_whenUserExistAndThereAreNoTopics_receivePageWithZeroItemsBeforeProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());
        Topic fourth = topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());

        userService.save(TestUtil.createValidUser("user2"));

        ResponseEntity<TestPage<TopicVM>> response = getOldTopicsOfUser(fourth.getId(), "user2", new ParameterizedTypeReference<TestPage<TopicVM>>() {});
        assertThat(response.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    public void getNewTopics_whenThereAreTopics_receiveListOfItemsAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());
        Topic fourth = topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());

        ResponseEntity<List<Object>> response = getNewTopics(fourth.getId(), new ParameterizedTypeReference<List<Object>>() {});
        assertThat(response.getBody().size()).isEqualTo(1);
    }

    @Test
    public void getNewTopics_whenThereAreTopics_receiveListOfTopicVMAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());
        Topic fourth = topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());

        ResponseEntity<List<TopicVM>> response = getNewTopics(fourth.getId(), new ParameterizedTypeReference<List<TopicVM>>() {});
        assertThat(response.getBody().get(0).getDate()).isGreaterThan(0);
    }

    @Test
    public void getNewTopicsOfUser_whenUserExistThereAreNoTopics_receiveOk() {
        userService.save(TestUtil.createValidUser("user1"));
        ResponseEntity<Object> response = getNewTopicsOfUser(5, "user1", new ParameterizedTypeReference<Object>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getNewTopicsOfUser_whenUserExistAndThereAreTopics_receiveListWithItemsAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());
        Topic fourth = topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());

        ResponseEntity<List<Object>> response = getNewTopicsOfUser(fourth.getId(), "user1", new ParameterizedTypeReference<List<Object>>() {});
        assertThat(response.getBody().size()).isEqualTo(1);
    }

    @Test
    public void getNewTopicsOfUser_whenUserExistAndThereAreTopics_receiveListWithTopicVMAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());
        Topic fourth = topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());

        ResponseEntity<List<TopicVM>> response = getNewTopicsOfUser(fourth.getId(), "user1", new ParameterizedTypeReference<List<TopicVM>>() {});
        assertThat(response.getBody().get(0).getDate()).isGreaterThan(0);
    }


    @Test
    public void getNewTopicsOfUser_whenUserDoesNotExistThereAreNoTopics_receiveNotFound() {
        ResponseEntity<Object> response = getNewTopicsOfUser(5, "user1", new ParameterizedTypeReference<Object>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getNewTopicsOfUser_whenUserExistAndThereAreNoTopics_receiveListWithZeroItemsAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());
        Topic fourth = topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());

        userService.save(TestUtil.createValidUser("user2"));

        ResponseEntity<List<TopicVM>> response = getNewTopicsOfUser(fourth.getId(), "user2", new ParameterizedTypeReference<List<TopicVM>>() {});
        assertThat(response.getBody().size()).isEqualTo(0);
    }

    @Test
    public void getNewTopicCount_whenThereAreTopics_receiveCountAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());
        Topic fourth = topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());

        ResponseEntity<Map<String, Long>> response = getNewTopicCount(fourth.getId(), new ParameterizedTypeReference<Map<String, Long>>() {});
        assertThat(response.getBody().get("count")).isEqualTo(1);
    }


    @Test
    public void getNewTopicCountOfUser_whenThereAreTopics_receiveCountAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());
        Topic fourth = topicService.save(user, TestUtil.createValidTopic());
        topicService.save(user, TestUtil.createValidTopic());

        ResponseEntity<Map<String, Long>> response = getNewTopicCountOfUser(fourth.getId(), "user1", new ParameterizedTypeReference<Map<String, Long>>() {});
        assertThat(response.getBody().get("count")).isEqualTo(1);
    }


    @Test
    public void deleteTopic_whenUserIsUnAuthorized_receiveUnauthorized() {
        ResponseEntity<Object> response = deleteTopic(555, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void deleteTopic_whenUserIsAuthorized_receiveOk() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Topic topic = topicService.save(user, TestUtil.createValidTopic());

        ResponseEntity<Object> response = deleteTopic(topic.getId(), Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    }

    @Test
    public void deleteTopic_whenUserIsAuthorized_receiveGenericResponse() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Topic topic = topicService.save(user, TestUtil.createValidTopic());

        ResponseEntity<GenericResponse> response = deleteTopic(topic.getId(), GenericResponse.class);
        assertThat(response.getBody().getMessage()).isNotNull();

    }

    @Test
    public void deleteTopic_whenUserIsAuthorized_topicRemovedFromDatabase() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Topic topic = topicService.save(user, TestUtil.createValidTopic());

        deleteTopic(topic.getId(), Object.class);
        Optional<Topic> inDB = topicRepository.findById(topic.getId());
        assertThat(inDB.isPresent()).isFalse();

    }

    @Test
    public void deleteTopic_whenTopicIsOwnedByAnotherUser_receiveForbidden() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        User topicOwner = userService.save(TestUtil.createValidUser("topic-owner"));
        Topic topic = topicService.save(topicOwner, TestUtil.createValidTopic());

        ResponseEntity<Object> response = deleteTopic(topic.getId(), Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    }

    @Test
    public void deleteTopic_whenTopicNotExist_receiveForbidden() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        ResponseEntity<Object> response = deleteTopic(5555, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    }


    public <T> ResponseEntity<T> deleteTopic(long topicId, Class<T> responseType){
        return testRestTemplate.exchange(API_1_0_TOPICS + "/" + topicId, HttpMethod.DELETE, null, responseType);
    }


    public <T> ResponseEntity<T> getNewTopicCount(long topicId, ParameterizedTypeReference<T> responseType){
        String path = API_1_0_TOPICS + "/" + topicId +"?direction=after&count=true";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getNewTopicCountOfUser(long topicId, String username, ParameterizedTypeReference<T> responseType){
        String path = "/api/1.0/users/" + username + "/topics/" + topicId +"?direction=after&count=true";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getNewTopics(long topicId, ParameterizedTypeReference<T> responseType){
        String path = API_1_0_TOPICS + "/" + topicId +"?direction=after&sort=id,desc";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getNewTopicsOfUser(long topicId, String username, ParameterizedTypeReference<T> responseType){
        String path = "/api/1.0/users/" + username + "/topics/" + topicId +"?direction=after&sort=id,desc";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getOldTopics(long topicId, ParameterizedTypeReference<T> responseType){
        String path = API_1_0_TOPICS + "/" + topicId +"?direction=before&page=0&size=5&sort=id,desc";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getOldTopicsOfUser(long topicId, String username, ParameterizedTypeReference<T> responseType){
        String path = "/api/1.0/users/" + username + "/topics/" + topicId +"?direction=before&page=0&size=5&sort=id,desc";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getTopicsOfUser(String username, ParameterizedTypeReference<T> responseType){
        String path = "/api/1.0/users/" + username + "/topics";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getTopics(ParameterizedTypeReference<T> responseType){
        return testRestTemplate.exchange(API_1_0_TOPICS, HttpMethod.GET, null, responseType);
    }


    private <T> ResponseEntity<T> postTopic(Topic topic, Class<T> responseType) {
        return testRestTemplate.postForEntity(API_1_0_TOPICS, topic, responseType);
    }


    private void authenticate(String username) {
        testRestTemplate.getRestTemplate()
                .getInterceptors().add(new BasicAuthenticationInterceptor(username, "P4ssword"));
    }
}
