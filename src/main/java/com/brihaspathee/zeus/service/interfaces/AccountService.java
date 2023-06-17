package com.brihaspathee.zeus.service.interfaces;

import com.brihaspathee.zeus.domain.entity.Transaction;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.account.EnrollmentSpanDto;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import com.brihaspathee.zeus.web.model.EnrollmentSpanStatusDto;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;
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
     * @param transactionDto the dto object that was received for processing the account
     * @param transaction the entity object that was persisted in APS
     * @return return the account dto object that was updated
     * @throws JsonProcessingException json processing exception
     */
    AccountDto createAccount(TransactionDto transactionDto, Transaction transaction) throws JsonProcessingException;

    /**
     * This method should be invoked if an account should be updated
     * Use this method if the calling method only has the account number of the account that needs to be updated
     * @param accountNumber Account number of the account that needs to be updated
     * @param transactionDto the dto object that was received for processing the account
     * @param transaction the entity object that was persisted in APS
     * @return return the account dto object that was updated
     * @throws JsonProcessingException json processing exception
     */
    AccountDto updateAccount(String accountNumber, TransactionDto transactionDto, Transaction transaction) throws JsonProcessingException;

    /**
     * This method should be invoked if an account should be updated.
     * Use this method if the calling method already has the account dto that needs to be updated
     * @param accountDto Account that needs to be updated
     * @param transactionDto the dto object that was received for processing the account
     * @param transaction the entity object that was persisted in APS
     * @return return the account dto object that was updated
     * @throws JsonProcessingException json processing exception
     */
    AccountDto updateAccount(AccountDto accountDto, TransactionDto transactionDto, Transaction transaction) throws JsonProcessingException;

    /**
     * Determine the status of the enrollment span
     * @param enrollmentSpanStatusDto enrollment span for which the status is to be determined
     * @return the status of the enrollment span
     */
    String determineEnrollmentSpanStatus(EnrollmentSpanStatusDto enrollmentSpanStatusDto);

}
