package com.brihaspathee.zeus.helper.interfaces;

import com.brihaspathee.zeus.domain.entity.Account;
import com.brihaspathee.zeus.domain.entity.EnrollmentSpan;
import com.brihaspathee.zeus.domain.entity.Member;
import com.brihaspathee.zeus.domain.repository.MemberEmailRepository;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;

import java.util.List;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 24, November 2022
 * Time: 6:22 AM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.helper.interfaces
 * To change this template use File | Settings | File and Code Template
 */
public interface EnrollmentSpanHelper {

    /**
     * Create an enrollment span from the transaction data
     * @param transactionDto
     * @param account
     * @return
     */
    EnrollmentSpan createEnrollmentSpan(TransactionDto transactionDto, Account account);

    /**
     * Set the enrollment span in the account dto to send to MMS
     * @param accountDto
     * @param account
     */
    void setEnrollmentSpan(AccountDto accountDto, Account account, String ztcn);
}
