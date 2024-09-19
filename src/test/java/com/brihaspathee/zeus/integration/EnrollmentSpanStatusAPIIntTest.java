package com.brihaspathee.zeus.integration;

import com.brihaspathee.zeus.dto.account.EnrollmentSpanStatusDto;
import com.brihaspathee.zeus.test.BuildTestData;
import com.brihaspathee.zeus.test.TestClass;
import com.brihaspathee.zeus.web.model.TestEnrollmentSpanStatusRequest;
import com.brihaspathee.zeus.web.response.ZeusApiResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 07, March 2023
 * Time: 2:12 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.integration
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EnrollmentSpanStatusAPIIntTest {

    /**
     * Object mapper to read the file and convert it to an object
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Rest template to call the api endpoint
     */
    @Autowired
    private TestRestTemplate testRestTemplate;

    /**
     * The file that contains the test data
     */
    @Value("classpath:com/brihaspathee/zeus/integration/EnrollmentSpanStatusAPIIntTest.json")
    Resource resourceFile;

    /**
     * The instance of the class that helps to build the input data
     */
    private TestClass<TestEnrollmentSpanStatusRequest> enrollmentSpanStatusRequestTestClass;

    /**
     * The instance of the class that helps to build the data
     */
    @Autowired
    private BuildTestData<TestEnrollmentSpanStatusRequest> buildTestData;

    /**
     * The list of test requests
     */
    private List<TestEnrollmentSpanStatusRequest> requests = new ArrayList<>();

    /**
     * The setup method is executed before each test method is executed
     * @param testInfo
     * @throws IOException
     */
    @BeforeEach
    void setUp(TestInfo testInfo) throws IOException {

        // Read the file information and convert to test class object
        enrollmentSpanStatusRequestTestClass = objectMapper.readValue(resourceFile.getFile(), new TypeReference<TestClass<TestEnrollmentSpanStatusRequest>>() {});

        // Build the test data for the test method that is to be executed
        this.requests = buildTestData.buildData(testInfo.getTestMethod().get().getName(),this.enrollmentSpanStatusRequestTestClass);
    }

    /**
     * This method tests the status of the enrollment span
     * @param repetitionInfo
     */
    @RepeatedTest(4)
    @Order(1)
    void testEnrollmentSpanStatus(RepetitionInfo repetitionInfo){
        log.info("Current Repetition:{}", repetitionInfo.getCurrentRepetition());

        // Retrieve the enrollment span request for the repetition
        TestEnrollmentSpanStatusRequest testEnrollmentSpanStatusRequest = requests.get(repetitionInfo.getCurrentRepetition() - 1);
        // validateAuthorityRequest(testAccountMatchRequest);
        log.info("Test enrollment span status request:{}", testEnrollmentSpanStatusRequest);
        String expectedEnrollmentSpanStatus = testEnrollmentSpanStatusRequest.getExpectedEnrollmentSpanStatus();
        EnrollmentSpanStatusDto inputEnrollmentSpanStatusDto = testEnrollmentSpanStatusRequest.getEnrollmentSpanStatusDto();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EnrollmentSpanStatusDto> httpEntity = new HttpEntity<>(inputEnrollmentSpanStatusDto, headers);
        String uri = "/api/v1/zeus/account-processor/span-status";
        // Call the API Endpoint to determine the status of the enrollment span
        ResponseEntity<ZeusApiResponse> responseEntity = testRestTemplate
                .postForEntity(uri,httpEntity, ZeusApiResponse.class);
        // Get the response body from the response
        String actualEnrollmentSpanStatus = responseEntity.getBody().getResponse().toString();
        log.info("Expected Enrollment Span Status:{}", expectedEnrollmentSpanStatus);
        log.info("Actual Enrollment Span Status:{}", actualEnrollmentSpanStatus);
        assertEquals(expectedEnrollmentSpanStatus, actualEnrollmentSpanStatus);
    }

}
