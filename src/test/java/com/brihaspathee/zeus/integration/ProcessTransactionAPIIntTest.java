package com.brihaspathee.zeus.integration;

import com.brihaspathee.zeus.broker.message.AccountProcessingRequest;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.test.BuildTestData;
import com.brihaspathee.zeus.test.TestClass;
import com.brihaspathee.zeus.test.validator.AccountValidation;
import com.brihaspathee.zeus.web.model.TestAccountProcessingRequest;
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

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 21, March 2023
 * Time: 2:28 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.integration
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProcessTransactionAPIIntTest {

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
    @Value("classpath:com/brihaspathee/zeus/integration/ProcessTransactionAPIIntTest.json")
    Resource resourceFile;

    /**
     * The instance of the class that helps to build the input data
     */
    private TestClass<TestAccountProcessingRequest> accountProcessingRequestTestClass;

    /**
     * The instance of the class that helps to build the data
     */
    @Autowired
    private BuildTestData<TestAccountProcessingRequest> buildTestData;

    /**
     * The account validation instance to validate the details of the account
     */
    private AccountValidation accountValidation = new AccountValidation();


    /**
     * The list of test requests
     */
    private List<TestAccountProcessingRequest> requests = new ArrayList<>();

    /**
     * The setup method is executed before each test method is executed
     * @param testInfo
     * @throws IOException
     */
    @BeforeEach
    void setUp(TestInfo testInfo) throws IOException {

        // Read the file information and convert to test class object
        accountProcessingRequestTestClass = objectMapper.readValue(resourceFile.getFile(), new TypeReference<TestClass<TestAccountProcessingRequest>>() {});

        // Build the test data for the test method that is to be executed
        this.requests = buildTestData.buildData(testInfo.getTestMethod().get().getName(),this.accountProcessingRequestTestClass);
    }

    /**
     * This method tests the processing of the transaction
     * @param repetitionInfo the repetition identifies the test iteration
     */
    @RepeatedTest(8)
    @Order(1)
    void testProcessTransaction(RepetitionInfo repetitionInfo){
        log.info("Current Repetition:{}", repetitionInfo.getCurrentRepetition());

        // Retrieve the accounting processing request for the repetition
        TestAccountProcessingRequest testAccountProcessingRequest = requests.get(repetitionInfo.getCurrentRepetition() - 1);
        log.info("Test accounting processing request:{}", testAccountProcessingRequest);
        AccountDto expectedAccountDto = testAccountProcessingRequest.getExpectedAccountDto();
        AccountProcessingRequest accountProcessingRequest = testAccountProcessingRequest.getAccountProcessingRequest();
        log.info("Entity Codes:{}", accountProcessingRequest.getTransactionDto().getEntityCodes());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AccountProcessingRequest> httpEntity = new HttpEntity<>(accountProcessingRequest, headers);
        String uri = "/api/v1/zeus/account-processor/process/false";
        // Call the API Endpoint to process the transaction
        ResponseEntity<ZeusApiResponse> responseEntity = testRestTemplate
                .postForEntity(uri,httpEntity, ZeusApiResponse.class);
        ZeusApiResponse apiResponse = responseEntity.getBody();
        // Get the account dto object
        AccountDto actualAccountDto =
                objectMapper.convertValue(apiResponse.getResponse(), AccountDto.class);
        log.info("Account Dto:{}", actualAccountDto);
        accountValidation.assertAccount(expectedAccountDto, actualAccountDto);
    }
}
