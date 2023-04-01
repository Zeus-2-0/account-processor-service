package com.brihaspathee.zeus.service.impl;

import com.brihaspathee.zeus.broker.message.AccountProcessingResponse;
import com.brihaspathee.zeus.broker.message.AccountUpdateRequest;
import com.brihaspathee.zeus.broker.producer.AccountUpdateProducer;
import com.brihaspathee.zeus.domain.entity.PayloadTracker;
import com.brihaspathee.zeus.domain.entity.Transaction;
import com.brihaspathee.zeus.domain.repository.TransactionRepository;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import com.brihaspathee.zeus.mapper.interfaces.TransactionMapper;
import com.brihaspathee.zeus.service.interfaces.AccountService;
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
     * The transaction repository instance to perform the CRUD operations
     */
    private final TransactionRepository transactionRepository;

    /**
     * The transaction mapper instance for mapping the transaction
     */
    private final TransactionMapper transactionMapper;

    /**
     * Producer instance to send Account information to MMS
     */
    private final AccountUpdateProducer accountUpdateProducer;

    /**
     * Process the transaction received to update/create an account in MMS
     * @param transactionDto
     * @param accountNumber
     * @param sendToMMS
     * @return
     * @throws JsonProcessingException
     */
    @Override
    public AccountDto processTransaction(TransactionDto transactionDto,
                                   String accountNumber,
                                   boolean sendToMMS) throws JsonProcessingException {
        Transaction transaction = transactionMapper.transactionDtoToTransaction(transactionDto);
        transaction = transactionRepository.save(transaction);
        if(accountNumber == null){
            // If the account number is null, a new account has to be created in MMS
            AccountDto accountDto =  accountService.createAccount(transactionDto, transaction);
            AccountUpdateRequest accountUpdateRequest = AccountUpdateRequest.builder()
                    .accountDto(accountDto)
                    .build();
            if(sendToMMS){
                accountUpdateProducer.updateAccount(accountUpdateRequest);
            }
            return accountDto;
        }
        return null;
    }

    /**
     * Process the transaction request that is received through Kafka topic
     * @param accountProcessingRequest
     * @param payloadTracker
     */
    @Override
    public Mono<AccountProcessingResponse> processTransaction(AccountProcessingRequest accountProcessingRequest, PayloadTracker payloadTracker) throws JsonProcessingException {
        processTransaction(accountProcessingRequest.getTransactionDto(),
                accountProcessingRequest.getAccountNumber(), true);

        AccountProcessingResponse accountProcessingResponse = AccountProcessingResponse.builder()
                .responseId(ZeusRandomStringGenerator.randomString(15))
                .requestPayloadId(payloadTracker.getPayloadId())
                .accountNumber(accountProcessingRequest.getAccountNumber())
                .ztcn(accountProcessingRequest.getTransactionDto().getZtcn())
                .build();
        return Mono.just(accountProcessingResponse).delayElement(Duration.ofSeconds(30));
    }
}
