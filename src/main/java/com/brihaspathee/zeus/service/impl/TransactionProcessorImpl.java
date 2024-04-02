package com.brihaspathee.zeus.service.impl;

import com.brihaspathee.zeus.broker.message.AccountProcessingResponse;
import com.brihaspathee.zeus.broker.message.AccountUpdateRequest;
import com.brihaspathee.zeus.broker.producer.AccountUpdateProducer;
import com.brihaspathee.zeus.domain.entity.PayloadTracker;
import com.brihaspathee.zeus.domain.entity.ProcessingRequest;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import com.brihaspathee.zeus.service.interfaces.AccountService;
import com.brihaspathee.zeus.service.interfaces.RequestService;
import com.brihaspathee.zeus.service.interfaces.TransactionProcessor;
import com.brihaspathee.zeus.broker.message.AccountProcessingRequest;
import com.brihaspathee.zeus.util.ZeusRandomStringGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

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
     * Process the transaction request that is received through Kafka topic
     * @param accountProcessingRequest
     * @param payloadTracker
     */
    @Override
    public Mono<AccountProcessingResponse> processTransaction(AccountProcessingRequest accountProcessingRequest, PayloadTracker payloadTracker) throws JsonProcessingException {
        // Process the transaction that was received
        AccountDto accountDto = processTransactionByAccountNumber(accountProcessingRequest.getTransactionDto(),
                accountProcessingRequest.getAccountNumber());
        // Send information to MMS to update the account
        sendUpdateToMMS(accountDto, payloadTracker.getPayloadId());
        // Create the processing response to send to TMS
        AccountProcessingResponse accountProcessingResponse = AccountProcessingResponse.builder()
                .responseId(ZeusRandomStringGenerator.randomString(15))
                .requestPayloadId(payloadTracker.getPayloadId())
                .accountNumber(accountProcessingRequest.getAccountNumber())
                .ztcn(accountProcessingRequest.getTransactionDto().getZtcn())
                .responseCode("8000002")
                .responseMessage("Processing Completed - Sent to MMS For Update")
                .build();
        return Mono.just(accountProcessingResponse);
//        return Mono.just(accountProcessingResponse).delayElement(Duration.ofSeconds(30));
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
                    accountProcessingRequest.getAccountNumber());
        }else{
            accountDto = processTransactionByAccountDto(accountProcessingRequest.getTransactionDto(),
                    accountProcessingRequest.getAccountDto());
        }
        if(sendToMMS){
            sendUpdateToMMS(accountDto, null);
        }
        return accountDto;
    }


    /**
     * Process the transaction received to update/create an account in MMS
     * @param transactionDto
     * @param accountNumber
     * @return
     * @throws JsonProcessingException
     */
    private AccountDto processTransactionByAccountNumber(TransactionDto transactionDto,
                                          String accountNumber) throws JsonProcessingException {
        ProcessingRequest processingRequest =
                requestService.saveRequest(transactionDto);
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
     * Process the transaction received to update/create an account in MMS
     * @param transactionDto
     * @param accountDto
     * @return
     * @throws JsonProcessingException
     */
    private AccountDto processTransactionByAccountDto(TransactionDto transactionDto,
                                          AccountDto accountDto) throws JsonProcessingException {
        ProcessingRequest processingRequest =
                requestService.saveRequest(transactionDto);
        return accountService.updateAccount(accountDto, transactionDto, processingRequest);
    }

}
