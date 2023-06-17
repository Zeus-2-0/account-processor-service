package com.brihaspathee.zeus.helper.interfaces;

import com.brihaspathee.zeus.domain.entity.Account;
import com.brihaspathee.zeus.domain.entity.EnrollmentSpan;
import com.brihaspathee.zeus.domain.entity.Member;
import com.brihaspathee.zeus.domain.repository.MemberEmailRepository;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.account.EnrollmentSpanDto;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
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
     * Get enrollment spans that are overlapping
     * @param accountDto The account from which the overlapping enrollment spans are to be identfied
     * @param effectiveStartDate the start date that is to be used
     * @param effectiveEndDate the end date that is to be used
     * @param coverageTypeCode identifies the type of coverage "FAM" or "DEP"
     * @return return the enrollment spans that are overlapping with the dates that are passed
     */
    List<EnrollmentSpanDto> getOverlappingEnrollmentSpans(AccountDto accountDto,
                                                          LocalDate effectiveStartDate,
                                                          LocalDate effectiveEndDate,
                                                          String coverageTypeCode);

    /**
     * Identify the enrollment spans that overlap the dates and update the dtos appropriately
     * @param overlappingEnrollmentSpans List of enrollment spans that overlap
     * @param effectiveStartDate the dates when the enrollment spans are overlapping
     * @param effectiveEndDate the dates when the enrollment spans are overlapping
     * @return return the enrollment spans the need to be termed or canceled to avoid overlapping issues
     */
    List<EnrollmentSpanDto> updateOverlappingEnrollmentSpans(List<EnrollmentSpanDto> overlappingEnrollmentSpans,
                                                            LocalDate effectiveStartDate,
                                                            LocalDate effectiveEndDate);

    /**
     * Save the updated enrollment spans
     * @param enrollmentSpanDtos enrollment spans that need to be saved
     * @param account the account to which the enrollment spans belong
     * @return saved enrollment spans
     */
    List<EnrollmentSpan> saveUpdatedEnrollmentSpans(List<EnrollmentSpanDto> enrollmentSpanDtos, Account account);

    /**
     * Get the enrollment spans that are immediately before the start date provided in the input
     * @param accountDto the account dto that contains the enrollment spans
     * @param startDate the start date before which the enrollment spans are requested
     * @param matchCancelSpans boolean to indicate of cancel spans should be considered a match
     * @return return the list of matched enrollment spans
     */
    List<EnrollmentSpanDto> getPriorEnrollmentSpans(AccountDto accountDto, LocalDate startDate, boolean matchCancelSpans);
}
