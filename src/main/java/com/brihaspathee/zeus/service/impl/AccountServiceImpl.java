package com.brihaspathee.zeus.service.impl;

import com.brihaspathee.zeus.broker.message.AccountProcessingResponse;
import com.brihaspathee.zeus.broker.message.AccountUpdateResponse;
import com.brihaspathee.zeus.broker.producer.AccountProcessingValidationProducer;
import com.brihaspathee.zeus.constants.ProcessFlowType;
import com.brihaspathee.zeus.domain.entity.*;
import com.brihaspathee.zeus.domain.repository.AccountRepository;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import com.brihaspathee.zeus.helper.interfaces.*;
import com.brihaspathee.zeus.mapper.interfaces.AccountMapper;
import com.brihaspathee.zeus.mapper.interfaces.ProcessingRequestMapper;
import com.brihaspathee.zeus.service.interfaces.AccountService;
import com.brihaspathee.zeus.service.interfaces.MemberManagementService;
import com.brihaspathee.zeus.util.AccountProcessorUtil;
import com.brihaspathee.zeus.validator.result.ProcessingValidationResult;
import com.brihaspathee.zeus.broker.message.AccountProcessingResult;
import com.brihaspathee.zeus.web.model.EnrollmentSpanStatusDto;
import com.brihaspathee.zeus.web.model.ProcessingRequestDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 23, November 2022
 * Time: 1:49 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.service.impl
 * To change this template use File | Settings | File and Code Template
 * Nuclino: <a href="https://app.nuclino.com/Balaji-Inc/Engineering-Wiki/Account-Service-31d66256-a4ed-45d9-9ae9-631f909902f1">Nuclino</a>
 * Confluence: <a href="https://vbalaji.atlassian.net/wiki/spaces/ZEUS/pages/99975169/APS+-+Account+Service">Confluence</a>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    /**
     * Account Repository instance to perform CRUD operations
     */
    private final AccountRepository accountRepository;

    /**
     * Account mapper instance for mapping the account
     */
    private final AccountMapper accountMapper;

    /**
     * Processing Request mapper instance for mapping the request
     */
    private final ProcessingRequestMapper processingRequestMapper;

    /**
     * The member helper instance
     */
    private final MemberHelper memberHelper;

    /**
     * The enrollment span helper instance
     */
    private final EnrollmentSpanHelper enrollmentSpanHelper;

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
     * Member management service instance to get information from MMS
     */
    private final MemberManagementService memberManagementService;

    /**
     * Add Transaction helper instance to process ADD transactions
     */
    private final AddTransactionHelper addTransactionHelper;

    /**
     * Change Transaction helper instance to process CHANGE transactions
     */
    private final ChangeTransactionHelper changeTransactionHelper;

    /**
     * Cancel/Term Transaction helper instance to cancel or term enrollment spans
     */
    private final CancelTermTransactionHelper cancelTermTransactionHelper;

    /**
     * Reinstatement Transaction helper instance to process REINSTATEMENT transactions
     */
    private final ReinstatementTransactionHelper reinstatementTransactionHelper;

    /**
     * The utility class for account processor service
     */
    private final AccountProcessorUtil accountProcessorUtil;

    /**
     * Processing validation producer to send the transaction for validation
     */
    private final AccountProcessingValidationProducer accountProcessingValidationProducer;

    /**
     * The spring environment instance
     */
    private final Environment environment;

    /**
     * This method should be invoked if a new account should be created
     * @param transactionDto The transaction dto from which the account has to be created
     * @param processingRequest the transaction entity to which the created account has to be associated
     * @return the created account
     * @throws JsonProcessingException the json processing exception
     */
    @Override
    public AccountDto createAccount(TransactionDto transactionDto, ProcessingRequest processingRequest) throws JsonProcessingException {
        String accountNumber = accountProcessorUtil.generateUniqueCode(transactionDto.getEntityCodes(),
                "accountNumber");
        // Create the account
        Account account = Account.builder()
//                .transaction(transaction)
                .processRequest(processingRequest)
                .matchFound(false)
                .accountNumber(accountNumber)
                .lineOfBusinessTypeCode(transactionDto.getTradingPartnerDto().getLineOfBusinessTypeCode())
                .ztcn(transactionDto.getZtcn())
                .source(transactionDto.getSource())
                .build();
        account = accountRepository.save(account);
        AccountDto accountDto = addTransactionHelper.updateAccount(null, account, transactionDto);
//        AccountDto accountDto = createAccountDto(account, processingRequest.getZrcn());
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.findAndRegisterModules();
//        log.info("Account to be sent to MMS:{}", objectMapper.writeValueAsString(accountDto));
        return accountDto;
    }

    /**
     * This method should be invoked if an account should be updated
     * @param accountNumber Account number of the account that needs to be updated
     * @param transactionDto the dto object that was received for processing the account
     * @param processingRequest the entity object that was persisted in APS
     * @return the account dto object that was updated
     * @throws JsonProcessingException json processing exception
     */
    @Override
    public AccountDto updateAccount(String accountNumber, TransactionDto transactionDto, ProcessingRequest processingRequest) throws JsonProcessingException {
        log.info("Calling MMS to get account for account number:{}", accountNumber);
        AccountDto accountDto = memberManagementService.getAccountByAccountNumber(accountNumber);
        return updateAccount(accountDto, transactionDto, processingRequest);
    }

    /**
     * This method should be invoked if an account should be updated
     * Use this method if the calling method only has the account number of the account that needs to be updated
     * @param accountDto Account number of the account that needs to be updated
     * @param transactionDto the dto object that was received for processing the account
     * @param processingRequest the entity object that was persisted in APS
     * @return the account dto object that was updated
     * @throws JsonProcessingException json processing exception
     */
    @Override
    public AccountDto updateAccount(AccountDto accountDto, TransactionDto transactionDto, ProcessingRequest processingRequest) throws JsonProcessingException {
        Account account = Account.builder()
//                .transaction(transaction)
                .processRequest(processingRequest)
                .matchFound(true)
                .matchAccountSK(accountDto.getAccountSK())
                .accountNumber(accountDto.getAccountNumber())
                .ztcn(accountDto.getZtcn())
                .source(accountDto.getSource())
                .lineOfBusinessTypeCode(transactionDto.getTradingPartnerDto().getLineOfBusinessTypeCode())
                .build();
        log.info("Account match sk before insert:{}", account.getMatchAccountSK());
        account = accountRepository.save(account);
        log.info("Account sk for account created in APS:{}", account.getAccountSK());
        // Check for the transaction type of the transaction and invoke the appropriate helper class
        String transactionTypeCode = transactionDto.getTransactionDetail().getTransactionTypeCode();
        if (transactionTypeCode.equals("ADD")){
            addTransactionHelper.updateAccount(accountDto, account, transactionDto);
        } else if (transactionTypeCode.equals("CHANGE")){
            changeTransactionHelper.updateAccount(accountDto, account, transactionDto);
        } else if (transactionTypeCode.equals("CANCELORTERM")){
            cancelTermTransactionHelper.updateAccount(accountDto, account, transactionDto);
//        }else if (transactionTypeCode.equals("TERM")){
//            termTransactionHelper.updateAccount(accountDto, account, transactionDto);
        }else if (transactionTypeCode.equals("REINSTATEMENT")){
            reinstatementTransactionHelper.updateAccount(accountDto, account, transactionDto);
        }

        return createAccountDto(account, transactionDto.getZtcn());
    }

    /**
     * Determine the status of the enrollment span
     * @param enrollmentSpanStatusDto
     * @return
     */
    @Override
    public String determineEnrollmentSpanStatus(EnrollmentSpanStatusDto enrollmentSpanStatusDto) {
        return enrollmentSpanHelper.determineStatus(enrollmentSpanStatusDto);
    }

    /**
     * Get account using account SK
     * @param accountSK
     * @return
     */
    @Override
    public Account getAccount(UUID accountSK) {
        return accountRepository.findById(accountSK).orElseThrow();
    }

    /**
     * Continue to process the transaction once the validations are completed
     * @param processingValidationResult
     * @return accountDto
     */
    @Transactional(propagation= Propagation.REQUIRED, readOnly=false, noRollbackFor=Exception.class)
    @Override
    public AccountProcessingResult postValidationProcessing(ProcessingValidationResult processingValidationResult)
            throws JsonProcessingException {
        ProcessFlowType processFlowType = processingValidationResult.getValidationRequest().getProcessFlowType();
        log.info("Process flow type is: {}", processFlowType);
        Account account = null;
        if(processFlowType.equals(ProcessFlowType.NEW_ACCOUNT) ||
                processFlowType.equals(ProcessFlowType.PLAN_CHANGE)){
            account = addTransactionHelper.postValidationProcessing(processingValidationResult);
        }else if(processFlowType.equals(ProcessFlowType.CHANGE)){
            account = changeTransactionHelper.postValidationProcessing(processingValidationResult);
        }else if(processFlowType.equals(ProcessFlowType.CANCEL_TERM)){
            account = cancelTermTransactionHelper .postValidationProcessing(processingValidationResult);
        }else if(processFlowType.equals(ProcessFlowType.REINSTATEMENT)){
            account = reinstatementTransactionHelper.postValidationProcessing(processingValidationResult);
        }
        AccountDto accountDto = createAccountDto(account, account.getProcessRequest().getZrcn());
        ProcessingRequestDto processingRequestDto = processingRequestMapper.
                processingRequestToProcessingRequestDto(account.getProcessRequest());
        return AccountProcessingResult.builder()
                .accountDto(accountDto)
                .processingRequestDto(processingRequestDto)
                .build();
    }

    /**
     * Continue to process the transaction once the MMS update is completed
     * @param processingRequest
     */
//    @Transactional(propagation= Propagation.REQUIRED, readOnly=false, noRollbackFor=Exception.class)
    @Override
    public AccountProcessingResult postMMSUpdate(ProcessingRequest processingRequest) {
        log.info("Inside account service to continue processing post mms update");
        Account account = processingRequest.getAccount();
        log.info("The account is:{}", account.getAccountNumber());
        AccountDto accountDto = createAccountDto(account, processingRequest.getZrcn());
        ProcessingRequestDto processingRequestDto = processingRequestMapper.
                processingRequestToProcessingRequestDto(account.getProcessRequest());
        return AccountProcessingResult.builder()
                .accountDto(accountDto)
                .processingRequestDto(processingRequestDto)
                .build();
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
}
