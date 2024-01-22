package com.brihaspathee.zeus.helper.impl;

import com.brihaspathee.zeus.domain.entity.Account;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import com.brihaspathee.zeus.helper.interfaces.CancelTransactionHelper;
import com.brihaspathee.zeus.helper.interfaces.EnrollmentSpanHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 04, December 2023
 * Time: 2:21 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.helper.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CancelTransactionHelperImpl implements CancelTransactionHelper {

    /**
     * Enrollment span helper to perform tasks that are associated with the enrollment span
     */
    private final EnrollmentSpanHelper enrollmentSpanHelper;

    /**
     * Update the account based on the transaction details
     * @param accountDto account information that was retrieved from MMS
     * @param account Account that needs to be updated
     * @param transactionDto the dto object that was received for processing the account
     */
    @Override
    public void updateAccount(AccountDto accountDto,
                                 Account account,
                                 TransactionDto transactionDto) throws JsonProcessingException {
        enrollmentSpanHelper.cancelEnrollmentSpan(accountDto, transactionDto, account);
    }
}
