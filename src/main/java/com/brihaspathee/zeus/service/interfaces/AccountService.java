package com.brihaspathee.zeus.service.interfaces;

import com.brihaspathee.zeus.domain.entity.Transaction;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import com.brihaspathee.zeus.web.model.EnrollmentSpanStatusDto;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.UUID;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 23, November 2022
 * Time: 1:24 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.service.interfaces
 * To change this template use File | Settings | File and Code Template
 */
public interface AccountService {

    /**
     * This method should be invoked if a new account should be created
     * @param transactionDto
     * @param transaction
     * @return
     * @throws JsonProcessingException
     */
    AccountDto createAccount(TransactionDto transactionDto, Transaction transaction) throws JsonProcessingException;

    /**
     * Determine the status of the enrollment span
     * @param enrollmentSpanStatusDto
     * @return
     */
    String determineEnrollmentSpanStatus(EnrollmentSpanStatusDto enrollmentSpanStatusDto);
}
