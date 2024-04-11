package com.brihaspathee.zeus.helper.impl;

import com.brihaspathee.zeus.broker.message.AccountUpdateRequest;
import com.brihaspathee.zeus.broker.producer.AccountProcessingValidationProducer;
import com.brihaspathee.zeus.broker.producer.AccountUpdateProducer;
import com.brihaspathee.zeus.constants.ProcessFlowType;
import com.brihaspathee.zeus.domain.entity.Account;
import com.brihaspathee.zeus.domain.entity.EnrollmentSpan;
import com.brihaspathee.zeus.domain.entity.Member;
import com.brihaspathee.zeus.domain.entity.ProcessingRequest;
import com.brihaspathee.zeus.domain.repository.AccountRepository;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.account.EnrollmentSpanDto;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import com.brihaspathee.zeus.helper.interfaces.*;
import com.brihaspathee.zeus.mapper.interfaces.AccountMapper;
import com.brihaspathee.zeus.service.interfaces.MemberManagementService;
import com.brihaspathee.zeus.validator.request.ProcessingValidationRequest;
import com.brihaspathee.zeus.validator.result.ProcessingValidationResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 23, May 2023
 * Time: 2:59 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.helper.impl
 * To change this template use File | Settings | File and Code Template
 * Nuclino: https://app.nuclino.com/Balaji-Inc/Engineering-Wiki/Add-Transaction-Helper-89637208-402f-469e-a399-3244d8f23b88
 * Confluence: https://vbalaji.atlassian.net/wiki/spaces/ZEUS/pages/99876887/Add+Transaction+Helper
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
     * Account mapper instance for mapping the account
     */
    private final AccountMapper accountMapper;

    /**
     * Instance of the account repository
     */
    private final AccountRepository accountRepository;

    /**
     * Member management service instance to get information from MMS
     */
    private final MemberManagementService memberManagementService;

    /**
     * Enrollment span helper instance to deal with all the operations in an enrollment span
     */
    private final EnrollmentSpanHelper enrollmentSpanHelper;

    /**
     * Member helper method to perform tasks that are associated with the member
     */
    private final MemberHelper memberHelper;

    /**
     * Broker helper instance to perform operations on the brokers
     */
    private final BrokerHelper brokerHelper;

    /**
     * Sponsor helper instance to perform operations on the sponsors
     */
    private final SponsorHelper sponsorHelper;

    /**
     * Payer helper instance to perform operations on the payers
     */
    private final PayerHelper payerHelper;

    /**
     * Processing validation producer to send the transaction for validation
     */
    private final AccountProcessingValidationProducer accountProcessingValidationProducer;

    /**
     * Producer instance to send Account information to MMS
     */
    private final AccountUpdateProducer accountUpdateProducer;

    /**
     * The spring environment instance
     */
    private final Environment environment;

    /**
     * Update the account based on the transaction details
     * @param accountDto Account information retrieved from MMS
     * @param account Account that needs to be updated
     * @param transactionDto the dto object that was received for processing the account
     */
    @Override
    public AccountDto updateAccount(AccountDto accountDto,
                                    Account account,
                                    TransactionDto transactionDto) throws JsonProcessingException {
        if(account.isMatchFound()){
            // This means there is an account already present in MMS and that updates have to be made
            // Get the all the existing enrollment span that will be affected when the transaction is completed processing
            List<EnrollmentSpanDto> overlappingEnrollmentSpans = enrollmentSpanHelper.getOverlappingEnrollmentSpans(accountDto,
                    transactionDto);
            // call the validation service to validate the transaction and account data before proceeding to process the
            // transaction -- Do not do this when running unit tests
            if(!Arrays.asList(environment.getActiveProfiles()).contains("test")){
                ProcessingRequest processingRequest = account.getProcessRequest();
                ProcessingValidationRequest validationRequest = ProcessingValidationRequest.builder()
                        .processFlowType(ProcessFlowType.PLAN_CHANGE)
                        .transactionDto(transactionDto)
                        .accountDto(AccountDto.builder()
                                .accountNumber(accountDto.getAccountNumber())
                                .enrollmentSpans(Set.copyOf(Optional.ofNullable(
                                                overlappingEnrollmentSpans)
                                        .orElse(Collections.emptyList())))
                                .build())
                        .accountSK(account.getAccountSK())
                        .processRequestSK(processingRequest.getProcessRequestSK())
                        .zrcnTypeCode(processingRequest.getZrcnTypeCode())
                        .zrcn(processingRequest.getZrcn())
                        .build();
                accountProcessingValidationProducer.sendAccountProcessingValidationRequest(validationRequest,
                        processingRequest.getRequestPayloadId());
                return null;
            }else{
                updateAccount(transactionDto, accountDto, account, overlappingEnrollmentSpans);
            }
        }else{
            // this means that the account is not present in the transaction and new account
            // has to be created in MMS
            // call the validation service to validate the transaction before proceeding to process the
            // transaction and creating the account for MMS -- Do not do this when running unit tests
            if(!Arrays.asList(environment.getActiveProfiles()).contains("test")){
                ProcessingRequest processingRequest = account.getProcessRequest();
                ProcessingValidationRequest validationRequest = ProcessingValidationRequest.builder()
                        .processFlowType(ProcessFlowType.NEW_ACCOUNT)
                        .transactionDto(transactionDto)
                        .accountDto(null)
                        .accountSK(account.getAccountSK())
                        .processRequestSK(processingRequest.getProcessRequestSK())
                        .zrcnTypeCode(processingRequest.getZrcnTypeCode())
                        .zrcn(processingRequest.getZrcn())
                        .build();
                accountProcessingValidationProducer.sendAccountProcessingValidationRequest(validationRequest,
                        processingRequest.getRequestPayloadId());
                return null;
            }else{
                createAccount(transactionDto, account);
            }
        }
        // This code will be reached during unit test for creating the account dto
        // This should not be reached during production because the process will send the data
        // to validation service asynchronously
        AccountDto updatedAccountDto = createAccountDto(account, account.getProcessRequest().getZrcn());
        // Use this piece of code when you need to print the account as JSON string in the logs
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.findAndRegisterModules();
//        String accountAsString = objectMapper.writeValueAsString(updatedAccountDto);
        return updatedAccountDto;
    }

    /**
     * Continue to process the transaction once the validations are completed
     * @param processingValidationResult
     */
    // @Transactional(propagation= Propagation.REQUIRED, readOnly=false, noRollbackFor=Exception.class)
    public Account postValidationProcessing(ProcessingValidationResult processingValidationResult) throws JsonProcessingException {
        log.info("Continuing to process transaction after the validation is completed");
        ProcessingValidationRequest request = processingValidationResult.getValidationRequest();
        ProcessFlowType processFlowType = request.getProcessFlowType();
        TransactionDto transactionDto = request.getTransactionDto();
        log.info("Retrieve the account belonging to this sk:{}", request.getAccountSK());
        Account account = accountRepository.getReferenceById(request.getAccountSK());
        log.info("Any members present in the account in APS:{}", account.getMembers());
        if(processFlowType.equals(ProcessFlowType.NEW_ACCOUNT)){
            // Continue to process the transaction after the validations are completed
            log.info("All rules have passed - Create the new account");
            createAccount(transactionDto, account);
        }else{
            // if not it is a plan change so process as below
            log.info("All rules have passed - Continue to perform plan change");
            Set<EnrollmentSpanDto> overlappingEnrollmentSpans = request.getAccountDto().getEnrollmentSpans();
            AccountDto accountDto = memberManagementService.getAccountByAccountNumber(request.getAccountDto().getAccountNumber());
            log.info("Account Dto returned from member management service:{}",accountDto);
            updateAccount(transactionDto, accountDto, account, List.copyOf(Optional.ofNullable(overlappingEnrollmentSpans)
                    .orElse(Collections.emptySet())));
        }
//        AccountDto accountDto = createAccountDto(account, account.getProcessRequest().getZrcn());
//        // Use this piece of code when you need to print the account as JSON string in the logs
////        ObjectMapper objectMapper = new ObjectMapper();
////        objectMapper.findAndRegisterModules();
////        String accountAsString = objectMapper.writeValueAsString(accountDto);
//        sendUpdateToMMS(accountDto, account.getProcessRequest().getRequestPayloadId());
        return account;
    }

    /**
     * Create the account since all the validations have passes
     * @param transactionDto
     * @param account
     * @throws JsonProcessingException
     */
    private void createAccount(TransactionDto transactionDto, Account account) throws JsonProcessingException {
        // Create the members. Members are created first before creating the enrollment spans so that the created
        // members can be passed to create the member premium records
        List<Member> members = memberHelper.createMembers(transactionDto.getMembers(), account);
        // Set the created members in the account object
        account.setMembers(members);
        // Create the enrollment span
        EnrollmentSpan enrollmentSpan =
                enrollmentSpanHelper.createEnrollmentSpan(transactionDto, account, null);
        account.setEnrollmentSpan(Arrays.asList(enrollmentSpan));
        // Create the sponsors from the transaction
        sponsorHelper.createSponsor(transactionDto, account);
        // Create the brokers from the transaction
        brokerHelper.createBroker(transactionDto, account);
        // Create the payers from the transaction
        payerHelper.createPayer(transactionDto, account);
//        AccountDto accountDto = createAccountDto(account, account.getProcessRequest().getZrcn());
//
//        sendUpdateToMMS(accountDto, account.getProcessRequest().getRequestPayloadId());
//        return accountDto;
    }

    /**
     * Update the account since all the validations have passed
     * @param transactionDto
     * @param accountDto
     * @param account
     * @param overlappingEnrollmentSpans
     * @throws JsonProcessingException
     */
    private void updateAccount(TransactionDto transactionDto,
                               AccountDto accountDto,
                               Account account,
                               List<EnrollmentSpanDto> overlappingEnrollmentSpans) throws JsonProcessingException {
        // Match the members in the transaction and create them in the APS repository
        memberHelper.matchMember(accountDto,transactionDto, account);
        enrollmentSpanHelper.updateEnrollmentSpans(accountDto, transactionDto, account, overlappingEnrollmentSpans);
//        AccountDto updatedAccountDto = createAccountDto(account, transactionDto.getZtcn());
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.findAndRegisterModules();
//        sendUpdateToMMS(updatedAccountDto, account.getProcessRequest().getRequestPayloadId());
//        return accountDto;
    }

    /**
     * Create the account dto to send to MMS
     * @param account
     */
    private AccountDto createAccountDto(Account account, String ztcn){
        AccountDto accountDto = accountMapper.accountToAccountDto(account);
        brokerHelper.setBroker(accountDto, account);
        payerHelper.setPayer(accountDto, account);
        sponsorHelper.setSponsor(accountDto, account);
        memberHelper.setMember(accountDto, account);
        enrollmentSpanHelper.setEnrollmentSpan(accountDto,
                account,
                ztcn);
        return accountDto;

    }

    /**
     * Send request to MMS service to update account
     * @param accountDto
     * @param parentPayloadId
     * @throws JsonProcessingException
     */
    private void sendUpdateToMMS(AccountDto accountDto, String parentPayloadId) throws JsonProcessingException {
        AccountUpdateRequest accountUpdateRequest = AccountUpdateRequest.builder()
                .accountDto(accountDto)
                .build();
        accountUpdateProducer.updateAccount(accountUpdateRequest, parentPayloadId);
    }


}
