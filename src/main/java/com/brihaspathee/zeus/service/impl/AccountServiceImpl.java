package com.brihaspathee.zeus.service.impl;

import com.brihaspathee.zeus.domain.entity.Account;
import com.brihaspathee.zeus.domain.entity.EnrollmentSpan;
import com.brihaspathee.zeus.domain.entity.Member;
import com.brihaspathee.zeus.domain.entity.Transaction;
import com.brihaspathee.zeus.domain.repository.AccountRepository;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import com.brihaspathee.zeus.helper.interfaces.*;
import com.brihaspathee.zeus.mapper.interfaces.AccountMapper;
import com.brihaspathee.zeus.service.interfaces.AccountService;
import com.brihaspathee.zeus.service.interfaces.MemberManagementService;
import com.brihaspathee.zeus.util.ZeusRandomStringGenerator;
import com.brihaspathee.zeus.web.model.EnrollmentSpanStatusDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 23, November 2022
 * Time: 1:49 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.service.impl
 * To change this template use File | Settings | File and Code Template
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
     * This method should be invoked if a new account should be created
     * @param transactionDto
     * @param transaction
     * @return
     * @throws JsonProcessingException
     */
    @Override
    public AccountDto createAccount(TransactionDto transactionDto, Transaction transaction) throws JsonProcessingException {
        // Create the account
        Account account = Account.builder()
                .transaction(transaction)
                .matchFound(false)
                .accountNumber(ZeusRandomStringGenerator.randomString(15))
                .lineOfBusinessTypeCode(transactionDto.getTradingPartnerDto().getLineOfBusinessTypeCode())
                .build();
        account = accountRepository.save(account);
        // Create the members. Members are created first before creating the enrollment spans so that the created
        // members can be passed to create the member premium records
        List<Member> members = memberHelper.createMember(transactionDto.getMembers(), account);
        // Set the created members in the account object
        account.setMembers(members);
        // Create the enrollment span
        EnrollmentSpan enrollmentSpan =
                enrollmentSpanHelper.createEnrollmentSpan(transactionDto, account);
        account.setEnrollmentSpan(Arrays.asList(enrollmentSpan));
        // Create the sponsors from the transaction
        sponsorHelper.createSponsor(transactionDto, account);
        // Create the brokers from the transaction
        brokerHelper.createBroker(transactionDto, account);
        // Create the payers from the transaction
        payerHelper.createPayer(transactionDto, account);
        AccountDto accountDto = createAccountDto(account, transaction.getZtcn());
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        log.info("Account to be set to MMS:{}", objectMapper.writeValueAsString(accountDto));
        return accountDto;
    }

    /**
     * This method should be invoked if an account should be updated
     * @param accountNumber Account number of the account that needs to be updated
     * @param transactionDto the dto object that was received for processing the account
     * @param transaction the entity object that was persisted in APS
     * @return the account dto object that was updated
     * @throws JsonProcessingException json processing exception
     */
    @Override
    public AccountDto updateAccount(String accountNumber, TransactionDto transactionDto, Transaction transaction) throws JsonProcessingException {
        AccountDto accountDto = memberManagementService.getAccountByAccountNumber(accountNumber);
        updateAccount(accountDto, transactionDto, transaction);
        return null;
    }

    /**
     * This method should be invoked if an account should be updated
     * Use this method if the calling method only has the account number of the account that needs to be updated
     * @param accountDto Account number of the account that needs to be updated
     * @param transactionDto the dto object that was received for processing the account
     * @param transaction the entity object that was persisted in APS
     * @return the account dto object that was updated
     * @throws JsonProcessingException json processing exception
     */
    @Override
    public AccountDto updateAccount(AccountDto accountDto, TransactionDto transactionDto, Transaction transaction) throws JsonProcessingException {
        addTransactionHelper.updateAccount(accountDto, transactionDto, transaction);
        return null;
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
