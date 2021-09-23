package com.carlsilber.tddredditbackend;

import com.carlsilber.tddredditbackend.domain.Topic;
import com.carlsilber.tddredditbackend.domain.User;
import com.carlsilber.tddredditbackend.error.ApiError;
import com.carlsilber.tddredditbackend.repositories.TopicRepository;
import com.carlsilber.tddredditbackend.repositories.UserRepository;
import com.carlsilber.tddredditbackend.services.TopicService;
import com.carlsilber.tddredditbackend.services.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.util.Map;
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

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    @Before
    public void cleanup() {
        topicRepository.deleteAll();
        userRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
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
