package com.brihaspathee.zeus.service.impl;

import com.brihaspathee.zeus.domain.entity.Account;
import com.brihaspathee.zeus.domain.entity.EnrollmentSpan;
import com.brihaspathee.zeus.domain.entity.Member;
import com.brihaspathee.zeus.domain.entity.Transaction;
import com.brihaspathee.zeus.domain.repository.AccountRepository;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import com.brihaspathee.zeus.helper.interfaces.*;
import com.brihaspathee.zeus.service.interfaces.AccountService;
import com.brihaspathee.zeus.util.ZeusRandomStringGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
     * This method should be invoked if a new account should be created
     * @param transactionDto
     * @param transaction
     */
    @Override
    public void createAccount(TransactionDto transactionDto, Transaction transaction) {
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
    }
}
