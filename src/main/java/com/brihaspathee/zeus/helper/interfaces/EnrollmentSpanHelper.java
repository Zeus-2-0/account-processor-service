package com.brihaspathee.zeus.helper.interfaces;

import com.brihaspathee.zeus.domain.entity.Account;
import com.brihaspathee.zeus.domain.entity.EnrollmentSpan;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.account.EnrollmentSpanDto;
import com.brihaspathee.zeus.dto.account.EnrollmentSpanStatusDto;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import com.brihaspathee.zeus.info.ChangeTransactionInfo;
import com.fasterxml.jackson.core.JsonProcessingException;

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
     * @param overlappingEnrollmentSpans
     */
    void updateEnrollmentSpans(AccountDto accountDto,
                               TransactionDto transactionDto,
                               Account account,
                               List<EnrollmentSpanDto> overlappingEnrollmentSpans) throws JsonProcessingException;

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

    /**
     * Cancel the enrollment span that is received in the transaction
     * @param matchedEnrollmentSpanDto
     * @param transactionDto
     * @param account
     */
    void cancelTermEnrollmentSpan(EnrollmentSpanDto matchedEnrollmentSpanDto,
                                  TransactionDto transactionDto, Account account);

    /**
     * Reinstate the enrollment span that is received in the transaction
     * @param accountDto
     * @param transactionDto
     * @param account
     */
    void reinstateEnrollmentSpan(AccountDto accountDto, TransactionDto transactionDto, Account account);

    /**
     * Get enrollment span that matches the group policy id
     * @param enrollmentSpanDtos
     * @param groupPolicyId
     * @return
     */
    EnrollmentSpanDto getMatchedEnrollmentSpan(Set<EnrollmentSpanDto> enrollmentSpanDtos, String groupPolicyId);

    /**
     * Get enrollment spans that are overlapping
     * @param accountDto The account from which the overlapping enrollment spans are to be identified
     * @param transactionDto the transaction that is being processed
     * @return return the enrollment spans that are overlapping with the dates that are passed
     */
    List<EnrollmentSpanDto> getOverlappingEnrollmentSpans(AccountDto accountDto,
                                                                  TransactionDto transactionDto);
}
