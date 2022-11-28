package com.brihaspathee.zeus.web.resource.impl;

import com.brihaspathee.zeus.service.interfaces.TransactionProcessor;
import com.brihaspathee.zeus.web.model.AccountProcessingRequest;
import com.brihaspathee.zeus.web.model.AccountProcessingResponse;
import com.brihaspathee.zeus.web.resource.interfaces.AccountProcessorAPI;
import com.brihaspathee.zeus.web.response.ZeusApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 23, November 2022
 * Time: 4:02 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.web.resource.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class AccountProcessorAPIImpl implements AccountProcessorAPI {

    /**
     * The transaction processor instance to process the transaction
     */
    private final TransactionProcessor transactionProcessor;

    /**
     * Process the transaction
     * @param accountProcessingRequest
     * @return
     * @throws JsonProcessingException
     */
    @Override
    public ResponseEntity<ZeusApiResponse<AccountProcessingResponse>> processTransaction(
            AccountProcessingRequest accountProcessingRequest) throws JsonProcessingException {
        log.info("Inside the account processor resource");
        transactionProcessor.processTransaction(accountProcessingRequest.getTransactionDto(),
                accountProcessingRequest.getAccountNumber());
        return null;
    }
}
