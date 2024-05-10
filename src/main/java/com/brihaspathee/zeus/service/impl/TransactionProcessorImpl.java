package com.brihaspathee.zeus.service.impl;

import com.brihaspathee.zeus.broker.message.*;
import com.brihaspathee.zeus.broker.message.request.BillingUpdateRequest;
import com.brihaspathee.zeus.broker.message.response.BillingUpdateResponse;
import com.brihaspathee.zeus.broker.producer.AccountProcessingResponseProducer;
import com.brihaspathee.zeus.broker.producer.AccountUpdateProducer;
import com.brihaspathee.zeus.broker.producer.BillingUpdateProducer;
import com.brihaspathee.zeus.domain.entity.PayloadTracker;
import com.brihaspathee.zeus.domain.entity.ProcessingRequest;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import com.brihaspathee.zeus.helper.interfaces.PayloadTrackerHelper;
import com.brihaspathee.zeus.service.interfaces.AccountService;
import com.brihaspathee.zeus.service.interfaces.RequestService;
import com.brihaspathee.zeus.service.interfaces.TransactionProcessor;
import com.brihaspathee.zeus.util.ZeusRandomStringGenerator;
import com.brihaspathee.zeus.validator.result.ProcessingValidationResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 23, November 2022
 * Time: 1:30 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.service.impl
 * To change this template use File | Settings | File and Code Template
 * Nuclino: https://app.nuclino.com/Balaji-Inc/Engineering-Wiki/Transaction-Processor-5758cdeb-0a24-403b-8632-c77835bd3228
 * Confluence: https://vbalaji.atlassian.net/wiki/spaces/ZEUS/pages/99876875/APS+-+Transaction+Processor
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionProcessorImpl implements TransactionProcessor {

    /**
     * Instance of the account service to create the details of the account
     */
    private final AccountService accountService;

    /**
     * Instance of the request service to save the request
     */
    private final RequestService requestService;

    /**
     * Producer instance to send Account information to MMS
     */
    private final AccountUpdateProducer accountUpdateProducer;

    /**
     * Producer instance to send billing update to Premium Billing
     */
    private final BillingUpdateProducer billingUpdateProducer;

    /**
     * Processing response producer instance to send the response back to TMS
     */
    private final AccountProcessingResponseProducer accountProcessingResponseProducer;

    /**
     * Process the transaction request that is received through Kafka topic
     * @param accountProcessingRequest
     * @param payloadTracker
     */
    @Override
    public Mono<String> processTransaction(AccountProcessingRequest accountProcessingRequest, PayloadTracker payloadTracker) throws JsonProcessingException {
        // Process the transaction that was received
        processTransactionByAccountNumber(accountProcessingRequest.getTransactionDto(),
                accountProcessingRequest.getAccountNumber(), payloadTracker.getPayloadId());
        // log.info("Account Dto to be sent to MMS:{}", accountDto);
        // Send information to MMS to update the account
        // sendUpdateToMMS(accountDto, payloadTracker.getPayloadId());
        // Create the processing response to send to TMS
//        AccountProcessingResponse accountProcessingResponse = AccountProcessingResponse.builder()
//                .responseId(ZeusRandomStringGenerator.randomString(15))
//                .requestPayloadId(payloadTracker.getPayloadId())
//                .accountNumber(accountProcessingRequest.getAccountNumber())
//                .ztcn(accountProcessingRequest.getTransactionDto().getZtcn())
//                .responseCode("8000002")
//                .responseMessage("Processing Completed - Sent to MMS For Update")
//                .build();
        sendProcessingUpdateToTM(payloadTracker.getPayloadId(),
                accountProcessingRequest.getTransactionDto().getZtcn(),
                accountProcessingRequest.getAccountNumber(),
                "8000001", "Processing in progress - Sent for validation");
//        return Mono.just(accountProcessingResponse);
//        return Mono.just(accountProcessingResponse).delayElement(Duration.ofSeconds(30));
        return Mono.just("Sent data for validation service");
    }

    /**
     * Process the transaction using accounting processing request
     * @param accountProcessingRequest the accounting processing request
     * @param sendToMMS identifies if the feed needs to be sent to MMS
     * @return returns the updated account
     * @throws JsonProcessingException generates json processing exception
     */
    @Override
    public AccountDto processTransaction(AccountProcessingRequest accountProcessingRequest, boolean sendToMMS) throws JsonProcessingException {
        AccountDto accountDto = null;
        if(accountProcessingRequest.getAccountDto() == null){
            accountDto = processTransactionByAccountNumber(accountProcessingRequest.getTransactionDto(),
                    accountProcessingRequest.getAccountNumber(), null);
        }else{
            accountDto = processTransactionByAccountDto(accountProcessingRequest.getTransactionDto(),
                    accountProcessingRequest.getAccountDto(), null);
        }
        if(sendToMMS){
            sendUpdateToMMS(accountDto, null);
        }
        return accountDto;
    }

    /**
     * Continue to process the transaction once the validations are completed
     * @param processingValidationResult
     */
    @Override
    public void postValidationProcessing(ProcessingValidationResult processingValidationResult)
            throws JsonProcessingException {
        AccountProcessingResult accountProcessingResult = accountService.postValidationProcessing(processingValidationResult);
        // Use this piece of code when you need to print the account as JSON string in the logs
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.findAndRegisterModules();
//        String accountAsString = objectMapper.writeValueAsString(accountDto);
        log.info("Request Payload id:{}", accountProcessingResult.getProcessingRequestDto().getRequestPayloadId());
        sendUpdateToMMS(accountProcessingResult.getAccountDto(),
                accountProcessingResult.getProcessingRequestDto().getRequestPayloadId());
        sendProcessingUpdateToTM(accountProcessingResult.getProcessingRequestDto().getRequestPayloadId(),
                accountProcessingResult.getProcessingRequestDto().getZrcn(),
                accountProcessingResult.getAccountDto().getAccountNumber(),
                "8000002", "Processing in progress - Sent to MMS For Update");

    }

    /**
     * Continue to process the transaction once the update to MMS is completed
     * @param accountUpdateResponse
     */
    @Transactional(propagation= Propagation.REQUIRED, readOnly=false, noRollbackFor=Exception.class)
    @Override
    public void postMMSUpdate(AccountUpdateResponse accountUpdateResponse) throws JsonProcessingException {
        log.info("about to process transaction for premium billing update");
        String requestPayloadId = accountUpdateResponse.getRequestPayloadId();
        ProcessingRequest processingRequest = requestService.getProcessingRequest(requestPayloadId);
        AccountProcessingResult accountProcessingResult = accountService.postMMSUpdate(processingRequest);
        AccountDto accountDto = accountProcessingResult.getAccountDto();
        // Check if the account that was updated has any enrollment spans to send the update to PB
        if(accountDto.getEnrollmentSpans() == null ||
        accountDto.getEnrollmentSpans().isEmpty()){
           // if there are no enrollment spans or if the enrollment span set is empty
           // there is no update to send to premium billing
           // so send the code 8000004 to TM to indicate that the processing is completed
            sendProcessingUpdateToTM(processingRequest.getRequestPayloadId(),
                    processingRequest.getZrcn(),
                    null,
                    "8000004", "Transaction Processing Completed");
        }else{
            // if there are enrollment spans then
            // send the account to PB for making necessary updates
            log.info("about to send data to premium billing service");
            sendUpdateToPB(accountProcessingResult);
            sendProcessingUpdateToTM(processingRequest.getRequestPayloadId(),
                    processingRequest.getZrcn(),
                    null,
                    "8000003", "Processing in progress - Sent to PB For Update");
        }

    }

    /**
     * Continue to process the transaction once the update to MMS is completed
     * @param billingUpdateResponse
     */
    @Transactional(propagation= Propagation.REQUIRED, readOnly=false, noRollbackFor=Exception.class)
    @Override
    public void postPBUpdate(BillingUpdateResponse billingUpdateResponse) throws JsonProcessingException {
        String requestPayloadId = billingUpdateResponse.getRequestPayloadId();
        log.info("Premium billing update is completed. The request payload id is:{}", requestPayloadId);

        ProcessingRequest processingRequest = requestService.getProcessingRequest(requestPayloadId);
        log.info("about to send status update to transaction manager:{}", processingRequest);
        sendProcessingUpdateToTM(processingRequest.getRequestPayloadId(),
                processingRequest.getZrcn(),
                null,
                "8000004", "Transaction Processing Completed");
    }


    /**
     * Process the transaction received to update/create an account in MMS
     * @param transactionDto
     * @param accountNumber
     * @return
     * @throws JsonProcessingException
     */
    private AccountDto processTransactionByAccountNumber(TransactionDto transactionDto,
                                          String accountNumber, String requestPayloadId) throws JsonProcessingException {
        ProcessingRequest processingRequest =
                requestService.saveRequest(transactionDto, requestPayloadId);
        AccountDto accountDto = null;
        if(accountNumber == null){
            // If the account number is null, a new account has to be created in MMS
            accountDto =  accountService.createAccount(transactionDto, processingRequest);
        }else{
            // If the account number is not null then update the account
            accountDto = accountService.updateAccount(accountNumber, transactionDto, processingRequest);
        }

//        if(sendToMMS){
//            accountUpdateProducer.updateAccount(accountUpdateRequest);
//        }
        return accountDto;
    }

    /**
     * Send processing update to Transaction Manager
     * @param requestPayloadId
     * @param ztcn
     * @param accountNumber
     * @param messageCode
     * @param messageDescription
     */
    private void sendProcessingUpdateToTM(String requestPayloadId,
                                          String ztcn,
                                          String accountNumber,
                                          String messageCode,
                                          String messageDescription) throws JsonProcessingException {
        AccountProcessingResponse accountProcessingResponse = AccountProcessingResponse.builder()
                .responseId(ZeusRandomStringGenerator.randomString(15))
                .requestPayloadId(requestPayloadId)
                .accountNumber(accountNumber)
                .ztcn(ztcn)
                .responseCode(messageCode)
                .responseMessage(messageDescription)
                .build();
        accountProcessingResponseProducer.sendAccountProcessingResponse(accountProcessingResponse);

    }

    /**
     * Send request to MMS service to update account
     * @param accountDto
     * @param parentPayloadId
     * @throws JsonProcessingException
     */
    private void sendUpdateToMMS(AccountDto accountDto, String parentPayloadId) throws JsonProcessingException {
        AccountUpdateRequest accountUpdateRequest = AccountUpdateRequest.builder()
                .accountDto(accountDto)
                .build();
        accountUpdateProducer.updateAccount(accountUpdateRequest, parentPayloadId);
    }

    /**
     * Send request to premium billing service to update billing account
     * @param accountProcessingResult
     * @throws JsonProcessingException
     */
    private void sendUpdateToPB(AccountProcessingResult accountProcessingResult) throws JsonProcessingException {
        BillingUpdateRequest billingUpdateRequest = BillingUpdateRequest.builder()
                .zrcn(accountProcessingResult.getProcessingRequestDto().getZrcn())
                .zrcnTypeCode(accountProcessingResult.getProcessingRequestDto().getZrcnTypeCode())
                .accountDto(accountProcessingResult.getAccountDto())
                .build();
        billingUpdateProducer.sendBillingUpdateValidationRequest(billingUpdateRequest,
                accountProcessingResult.getProcessingRequestDto().getRequestPayloadId());
    }

    /**
     * Process the transaction received to update/create an account in MMS
     * @param transactionDto
     * @param accountDto
     * @return
     * @throws JsonProcessingException
     */
    private AccountDto processTransactionByAccountDto(TransactionDto transactionDto,
                                          AccountDto accountDto, String requestPayloadId) throws JsonProcessingException {
        ProcessingRequest processingRequest =
                requestService.saveRequest(transactionDto, requestPayloadId);
        return accountService.updateAccount(accountDto, transactionDto, processingRequest);
    }

}
