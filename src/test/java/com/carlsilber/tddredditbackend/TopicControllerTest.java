package com.carlsilber.tddredditbackend;

import com.carlsilber.tddredditbackend.domain.Topic;
import com.carlsilber.tddredditbackend.error.ApiError;
import com.carlsilber.tddredditbackend.repositories.UserRepository;
import com.carlsilber.tddredditbackend.services.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

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

    @Before
    public void cleanup() {
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

    private <T> ResponseEntity<T> postTopic(Topic topic, Class<T> responseType) {
        return testRestTemplate.postForEntity(API_1_0_TOPICS, topic, responseType);
    }


    private void authenticate(String username) {
        testRestTemplate.getRestTemplate()
                .getInterceptors().add(new BasicAuthenticationInterceptor(username, "P4ssword"));
    }
}
