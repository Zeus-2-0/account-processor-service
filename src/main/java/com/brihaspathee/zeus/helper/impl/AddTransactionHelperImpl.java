package com.brihaspathee.zeus.helper.impl;

import com.brihaspathee.zeus.domain.entity.EnrollmentSpan;
import com.brihaspathee.zeus.domain.entity.Transaction;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.account.EnrollmentSpanDto;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import com.brihaspathee.zeus.helper.interfaces.AddTransactionHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 23, May 2023
 * Time: 2:59 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.helper.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AddTransactionHelperImpl implements AddTransactionHelper {

    /**
     * object mapper to print the object as json strings
     */
    private final ObjectMapper objectMapper;

    /**
     * Update the account based on the transaction details
     * @param accountDto Account that needs to be updated
     * @param transactionDto the dto object that was received for processing the account
     * @param transaction the entity object that was persisted in APS
     * @return the account dto object that was updated
     */
    @Override
    public AccountDto updateAccount(AccountDto accountDto, TransactionDto transactionDto, Transaction transaction) throws JsonProcessingException {
        LocalDate effectiveStartDate = transactionDto.getTransactionDetail().getEffectiveDate();
        LocalDate effectiveEndDate = transactionDto.getTransactionDetail().getEndDate();
        if(effectiveEndDate == null){
            effectiveEndDate = LocalDate.of(effectiveStartDate.getYear(), 12, 31);
        }
        // Get the enrollment span if any are affected
        Set<EnrollmentSpanDto> overlappingEnrollmentSpans = getOverlappingEnrollmentSpans(accountDto,
                effectiveStartDate,
                effectiveEndDate);
        if(overlappingEnrollmentSpans != null && !overlappingEnrollmentSpans.isEmpty()){
            // Get the enrollment span in which the effective date is falling in between its start date and end date
            Set<EnrollmentSpanDto> termCancelEnrollmentSpans = overlappingEnrollmentSpans.stream().filter(enrollmentSpanDto ->
                    enrollmentSpanDto.getStartDate().isEqual(effectiveStartDate) ||
                            (enrollmentSpanDto.getStartDate().isBefore(effectiveStartDate) &&
                            enrollmentSpanDto.getEndDate().isAfter(effectiveStartDate))).collect(Collectors.toSet());
            if(termCancelEnrollmentSpans.size() > 1){
                log.info("More than one enrollment spans to be termed");
            }else if (termCancelEnrollmentSpans.size() == 1){
                // Get the first element from the set
                final EnrollmentSpanDto termCancelEnrollmentSpan = termCancelEnrollmentSpans.stream().findFirst().get();
                overlappingEnrollmentSpans.forEach(enrollmentSpanDto -> {
                    if(enrollmentSpanDto.getEnrollmentSpanCode().equals(termCancelEnrollmentSpan.getEnrollmentSpanCode())){
                        if(enrollmentSpanDto.getStartDate().isBefore(effectiveStartDate)){
                            enrollmentSpanDto.setEndDate(effectiveStartDate.minusDays(1));
                        }else{
                            enrollmentSpanDto.setEndDate(enrollmentSpanDto.getStartDate());
                            enrollmentSpanDto.setStatusTypeCode("CANCELED");
                        }
                    }else{
                        enrollmentSpanDto.setEndDate(enrollmentSpanDto.getStartDate());
                        enrollmentSpanDto.setStatusTypeCode("CANCELED");
                    }
                });
            }
        }
        String overlappingSpanString = objectMapper.writeValueAsString(overlappingEnrollmentSpans);
        log.info("Overlapping enrollment spans updated:{}", overlappingSpanString);
        // Check if there are any enrollment spans that need to be termed or canceled
        return null;
    }

    /**
     * Get all the affected enrollment spans
     * @param accountDto Account that contains the enrollment spans
     * @param effectiveStartDate effective start date received in the transaction
     * @param effectiveEndDate effective end date received in the transaction
     * @return enrollment spans that will be affected based on the start and end date
     */
    private Set<EnrollmentSpanDto> getOverlappingEnrollmentSpans(AccountDto accountDto,
                                                              LocalDate effectiveStartDate,
                                                              LocalDate effectiveEndDate){
        if(accountDto.getEnrollmentSpans() == null || accountDto.getEnrollmentSpans().isEmpty()){
            return null;
        }
        Set<EnrollmentSpanDto> overlappingEnrollmentSpans = new HashSet<>();
        Set<EnrollmentSpanDto> enrollmentSpanDtos = accountDto.getEnrollmentSpans();
        int effectiveYear = effectiveStartDate.getYear();
        // Get all the enrollment spans that are present for the year for which the effective date is received
        // i.e. if the effective date is 2/1/2023 get all the enrollment spans that belong to the year 2023
        overlappingEnrollmentSpans = enrollmentSpanDtos.stream()
                .filter(enrollmentSpanDto -> enrollmentSpanDto.getStartDate().getYear() == effectiveYear)
                .collect(Collectors.toSet());
        // Get all the enrollment spans that have the end date that is greater than the effective start date
        // This eliminates all the enrollment spans that are termed prior to the effective start date of the transaction
        overlappingEnrollmentSpans = overlappingEnrollmentSpans.stream()
                .filter(enrollmentSpanDto -> enrollmentSpanDto.getEndDate().isAfter(effectiveStartDate))
                .collect(Collectors.toSet());
        // Get all enrollment spans that have the start date that is before the effective end date received in the
        // transaction
        overlappingEnrollmentSpans = overlappingEnrollmentSpans.stream()
                .filter(enrollmentSpanDto -> {
                    if(enrollmentSpanDto.getStatusTypeCode().equals("CANCELED")){
                        return false;
                    }
                    LocalDate enrollmentSpanStartDate = enrollmentSpanDto.getStartDate();
                    return enrollmentSpanStartDate.isBefore(effectiveEndDate);
                })
                .collect(Collectors.toSet());
        // Return null if there are no overlapping enrollment spans
        if(overlappingEnrollmentSpans.isEmpty()){
            return null;
        }else {
            return overlappingEnrollmentSpans;
        }
    }
}
