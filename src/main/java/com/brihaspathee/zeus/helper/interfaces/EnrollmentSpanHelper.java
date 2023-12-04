package com.brihaspathee.zeus.helper.interfaces;

import com.brihaspathee.zeus.domain.entity.Account;
import com.brihaspathee.zeus.domain.entity.EnrollmentSpan;
import com.brihaspathee.zeus.domain.entity.Member;
import com.brihaspathee.zeus.domain.repository.MemberEmailRepository;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.account.EnrollmentSpanDto;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberDto;
import com.brihaspathee.zeus.info.ChangeTransactionInfo;
import com.brihaspathee.zeus.web.model.EnrollmentSpanStatusDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

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
     * @param transactionDto The transaction detail
     * @param account the account to which the enrollment span should be associated
     * @param priorEnrollmentSpans enrollment spans that is immediately prior to the effective date in the transaction
     * @return return the created enrollment span
     */
    EnrollmentSpan createEnrollmentSpan(TransactionDto transactionDto, Account account, List<EnrollmentSpanDto> priorEnrollmentSpans);

    /**
     * Set the enrollment span in the account dto to send to MMS
     * @param accountDto
     * @param account
     */
    void setEnrollmentSpan(AccountDto accountDto, Account account, String ztcn);

    /**
     * Determine the status of the enrollment span
     * @param enrollmentSpanStatusDto
     * @return
     */
    String determineStatus(EnrollmentSpanStatusDto enrollmentSpanStatusDto);

    /**
     * Determine the enrollment span status
     * @param currentEnrollmentSpan
     * @param priorEnrollmentSpans
     * @return
     */
    String determineEnrollmentSpanStatus(EnrollmentSpan currentEnrollmentSpan,
                                         List<EnrollmentSpanDto> priorEnrollmentSpans);


    /**
     * Update the impacted enrollment spans and create ones as needed
     * @param accountDto
     * @param transactionDto
     * @param account
     */
    void updateEnrollmentSpans(AccountDto accountDto, TransactionDto transactionDto, Account account);

    /**
     * Process the financial change for the enrollment span
     * @param changeTransactionInfo - Details associated with the change transaction
     * @param transactionDto - Change transaction data
     * @param account - The account entity
     * @param accountDto - The account for which the transaction is received
     * @param matchedEnrollmentSpanDto - The matched enrollment span
     */
    void processFinancialChange(ChangeTransactionInfo changeTransactionInfo,
                                TransactionDto transactionDto,
                                Account account,
                                AccountDto accountDto,
                                EnrollmentSpanDto matchedEnrollmentSpanDto);
}
