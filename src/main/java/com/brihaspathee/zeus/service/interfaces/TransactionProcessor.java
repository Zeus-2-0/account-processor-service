package com.brihaspathee.zeus.service.interfaces;

import com.brihaspathee.zeus.broker.message.AccountProcessingResponse;
import com.brihaspathee.zeus.domain.entity.PayloadTracker;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import com.brihaspathee.zeus.broker.message.AccountProcessingRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import reactor.core.publisher.Mono;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 23, November 2022
 * Time: 1:23 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.service.interfaces
 * To change this template use File | Settings | File and Code Template
 */
public interface TransactionProcessor {

    /**
     * Process the transaction using accounting processing request
     * @param accountProcessingRequest the accounting processing request
     * @param payloadTracker the payload tracker object
     * @return returns the account processing response
     * @throws JsonProcessingException generates json processing exception
     */
    Mono<AccountProcessingResponse> processTransaction(AccountProcessingRequest accountProcessingRequest,
                                                       PayloadTracker payloadTracker) throws JsonProcessingException;

    /**
     * Process the transaction using accounting processing request
     * @param accountProcessingRequest the accounting processing request
     * @param sendToMMS identifies if the feed needs to be sent to MMS
     * @return returns the updated account
     * @throws JsonProcessingException generates json processing exception
     */
    AccountDto processTransaction(AccountProcessingRequest accountProcessingRequest,
                                  boolean sendToMMS) throws JsonProcessingException;
}
