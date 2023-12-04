package com.brihaspathee.zeus.helper.interfaces;

import com.brihaspathee.zeus.domain.entity.Account;
import com.brihaspathee.zeus.domain.entity.Transaction;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 27, November 2023
 * Time: 5:32 AM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.helper.interfaces
 * To change this template use File | Settings | File and Code Template
 */
public interface ChangeTransactionHelper {

    /**
     * Update the account based on the transaction details
     * @param accountDto account information that was retrieved from MMS
     * @param account Account that needs to be updated
     * @param transactionDto the dto object that was received for processing the account
     * @param transaction the entity object that was persisted in APS
     * @return the account dto object that was updated
     */
    Account updateAccount(AccountDto accountDto,
                          Account account,
                          TransactionDto transactionDto,
                          Transaction transaction) throws JsonProcessingException;
}
