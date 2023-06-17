package com.brihaspathee.zeus.helper.impl;

import com.brihaspathee.zeus.domain.entity.Account;
import com.brihaspathee.zeus.domain.entity.EnrollmentSpan;
import com.brihaspathee.zeus.domain.entity.Transaction;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.account.EnrollmentSpanDto;
import com.brihaspathee.zeus.dto.account.PremiumSpanDto;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import com.brihaspathee.zeus.helper.interfaces.AddTransactionHelper;
import com.brihaspathee.zeus.helper.interfaces.EnrollmentSpanHelper;
import com.brihaspathee.zeus.helper.interfaces.MemberHelper;
import com.brihaspathee.zeus.util.ZeusRandomStringGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
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
     * Enrollment span helper instance to deal with all the operations in an enrollment span
     */
    private final EnrollmentSpanHelper enrollmentSpanHelper;

    /**
     * Member helper method to perform tasks that are associated with the member
     */
    private final MemberHelper memberHelper;

    /**
     * Update the account based on the transaction details
     * @param accountDto Account information retrieved from MMS
     * @param account Account that needs to be updated
     * @param transactionDto the dto object that was received for processing the account
     * @param transaction the entity object that was persisted in APS
     * @return the account object that was updated
     */
    @Override
    public Account updateAccount(AccountDto accountDto,
                                    Account account,
                                    TransactionDto transactionDto,
                                    Transaction transaction) throws JsonProcessingException {
        // Match the members in the transaction and create them in the APS repository
        memberHelper.matchMember(accountDto,transactionDto, account);
        LocalDate effectiveStartDate = transactionDto.getTransactionDetail().getEffectiveDate();
        LocalDate effectiveEndDate = transactionDto.getTransactionDetail().getEndDate();
        if(effectiveEndDate == null){
            effectiveEndDate = LocalDate.of(effectiveStartDate.getYear(), 12, 31);
        }
        // Get the enrollment span if any are affected
        List<EnrollmentSpanDto> overlappingEnrollmentSpans = enrollmentSpanHelper.getOverlappingEnrollmentSpans(accountDto,
                effectiveStartDate,
                effectiveEndDate, transactionDto.getTransactionDetail().getCoverageTypeCode());
        // Get the overlapping enrollment spans updated appropriately.
        // Note this will just update within the DTO and not in the DB
        overlappingEnrollmentSpans = enrollmentSpanHelper.updateOverlappingEnrollmentSpans(
                overlappingEnrollmentSpans,
                effectiveStartDate,
                effectiveEndDate);
        updateAccountDtoWithOverlappingSpans(accountDto, overlappingEnrollmentSpans);
        List<EnrollmentSpan> updatedEnrollmentSpans = enrollmentSpanHelper.saveUpdatedEnrollmentSpans(overlappingEnrollmentSpans,
                account);
        if(updatedEnrollmentSpans == null){
            updatedEnrollmentSpans = new ArrayList<>();
        }
        updatedEnrollmentSpans.forEach(enrollmentSpan -> {
            log.info("Saved Enrollment span code before :{}", enrollmentSpan.getEnrollmentSpanCode());
            log.info("Saved Enrollment span ztcn before:{}", enrollmentSpan.getZtcn());
        });
        EnrollmentSpan newEnrollmentSpan = enrollmentSpanHelper.createEnrollmentSpan(transactionDto,
                account,
                enrollmentSpanHelper.getPriorEnrollmentSpans(accountDto, effectiveStartDate, false));
        updatedEnrollmentSpans.add(newEnrollmentSpan);
        updatedEnrollmentSpans.forEach(enrollmentSpan -> {
            log.info("Saved Enrollment span code after :{}", enrollmentSpan.getEnrollmentSpanCode());
            log.info("Saved Enrollment span ztcn after:{}", enrollmentSpan.getZtcn());
        });
        account.setEnrollmentSpan(updatedEnrollmentSpans);
//        String updatedAccountString = objectMapper.writeValueAsString(account);
//        log.info("Updated Account:{}", updatedAccountString);
        return account;
    }

    /**
     * The account dto object will be updated with the overlapping enrollment spans
     * @param accountDto The account dto that is to be updated
     * @param overlappingEnrollmentSpans the overlapping enrollment spans that needs to be added back to the account
     */
    private void updateAccountDtoWithOverlappingSpans(AccountDto accountDto,
                                                      List<EnrollmentSpanDto> overlappingEnrollmentSpans) {
        if(overlappingEnrollmentSpans == null || overlappingEnrollmentSpans.isEmpty()){
            return;
        }else{
            // todo add the overlapping enrollment spans
            Set<EnrollmentSpanDto> accountEnrollmentSpans = accountDto.getEnrollmentSpans();
            overlappingEnrollmentSpans.forEach(enrollmentSpanDto -> {
                Optional<EnrollmentSpanDto> optionalEnrollmentSpan = accountEnrollmentSpans.stream()
                        .filter(
                                accountEnrollmentSpan ->
                                        accountEnrollmentSpan.getEnrollmentSpanCode().equals(
                                                enrollmentSpanDto.getEnrollmentSpanCode()))
                        .findFirst();
                optionalEnrollmentSpan.ifPresent(accountEnrollmentSpans::remove);

            });
            accountEnrollmentSpans.addAll(overlappingEnrollmentSpans);
        }
    }


}
