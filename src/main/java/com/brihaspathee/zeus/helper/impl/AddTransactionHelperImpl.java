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
 * Nuclino: <a href="https://app.nuclino.com/Balaji-Inc/Engineering-Wiki/Add-Transaction-Helper-89637208-402f-469e-a399-3244d8f23b88>Nuclino</a>
 * Confluence: <a href="https://vbalaji.atlassian.net/wiki/spaces/ZEUS/pages/99876887/Add+Transaction+Helper>Confluence</a>
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
        enrollmentSpanHelper.updateEnrollmentSpans(accountDto, transactionDto, account);
        return account;
    }


}
