package com.brihaspathee.zeus.helper.impl;

import com.brihaspathee.zeus.broker.producer.AccountProcessingValidationProducer;
import com.brihaspathee.zeus.constants.ProcessFlowType;
import com.brihaspathee.zeus.domain.entity.Account;
import com.brihaspathee.zeus.domain.entity.ProcessingRequest;
import com.brihaspathee.zeus.domain.repository.AccountRepository;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.account.EnrollmentSpanDto;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import com.brihaspathee.zeus.helper.interfaces.CancelTermTransactionHelper;
import com.brihaspathee.zeus.helper.interfaces.EnrollmentSpanHelper;
import com.brihaspathee.zeus.service.interfaces.MemberManagementService;
import com.brihaspathee.zeus.validator.request.ProcessingValidationRequest;
import com.brihaspathee.zeus.validator.result.ProcessingValidationResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 05, April 2024
 * Time: 1:28 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.helper.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CancelTermTransactionHelperImpl implements CancelTermTransactionHelper {

    /**
     * Enrollment span helper to perform tasks that are associated with the enrollment span
     */
    private final EnrollmentSpanHelper enrollmentSpanHelper;

    /**
     * Processing validation producer to send the transaction for validation
     */
    private final AccountProcessingValidationProducer accountProcessingValidationProducer;

    /**
     * The spring environment instance
     */
    private final Environment environment;

    /**
     * Instance of the account repository
     */
    private final AccountRepository accountRepository;

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
        EnrollmentSpanDto matchedEnrollmentSpan = enrollmentSpanHelper.getMatchedEnrollmentSpan(
                accountDto.getEnrollmentSpans(),
                transactionDto.getTransactionDetail().getGroupPolicyId());
        // Send transaction for validation -- Do not do this when running unit tests
        if(!Arrays.asList(environment.getActiveProfiles()).contains("test")){
            // If this is not unit testing then first perform validations
            // by sending the transaction to rules service
            ProcessingRequest processingRequest = account.getProcessRequest();
            ProcessingValidationRequest validationRequest = ProcessingValidationRequest.builder()
                    .processFlowType(ProcessFlowType.CANCEL_TERM)
                    .transactionDto(transactionDto)
                    .accountDto(AccountDto.builder()
                            .accountNumber(accountDto.getAccountNumber())
                            .enrollmentSpans(Set.copyOf(Optional.of(
                                            Collections.singletonList(matchedEnrollmentSpan))
                                    .orElse(Collections.emptyList())))
                            .build())
                    .accountSK(account.getAccountSK())
                    .processRequestSK(processingRequest.getProcessRequestSK())
                    .zrcnTypeCode(processingRequest.getZrcnTypeCode())
                    .zrcn(processingRequest.getZrcn())
                    .build();
            accountProcessingValidationProducer.sendAccountProcessingValidationRequest(validationRequest
                    , processingRequest.getRequestPayloadId());
        }else{
            // if this is unit testing, continue to cancel or term the enrollment span
            cancelTermEnrollmentSpan(matchedEnrollmentSpan,
                    transactionDto,
                    account);
        }
//        enrollmentSpanHelper.cancelTermEnrollmentSpan(matchedEnrollmentSpan,
//                transactionDto,
//                account);

    }

    /**
     * Continue to process the transaction once the validations are completed
     * @param processingValidationResult
     */
    @Override
    public Account postValidationProcessing(ProcessingValidationResult processingValidationResult) throws JsonProcessingException {
        ProcessingValidationRequest request = processingValidationResult.getValidationRequest();
        TransactionDto transactionDto = request.getTransactionDto();
        Account account = accountRepository.getReferenceById(request.getAccountSK());
        // retrieve the matched enrollment span that was sent in the validation request
        EnrollmentSpanDto matchedEnrollmentSpan = request.getAccountDto()
                .getEnrollmentSpans()
                .stream()
                .findFirst()
                .orElseThrow();
        cancelTermEnrollmentSpan(matchedEnrollmentSpan, transactionDto, account);
        return account;
    }

    /**
     * Continue to cancel or term the enrollment span
     * @param matchedEnrollmentSpan
     * @param transactionDto
     * @param account
     */
    private void cancelTermEnrollmentSpan(EnrollmentSpanDto matchedEnrollmentSpan,
                                          TransactionDto transactionDto,
                                          Account account){
        enrollmentSpanHelper.cancelTermEnrollmentSpan(matchedEnrollmentSpan,
                transactionDto,
                account);
    }
}
