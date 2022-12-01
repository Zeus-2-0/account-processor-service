package com.brihaspathee.zeus.service.impl;

import com.brihaspathee.zeus.domain.entity.Transaction;
import com.brihaspathee.zeus.domain.repository.TransactionRepository;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import com.brihaspathee.zeus.mapper.interfaces.TransactionMapper;
import com.brihaspathee.zeus.service.interfaces.AccountService;
import com.brihaspathee.zeus.service.interfaces.TransactionProcessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 23, November 2022
 * Time: 1:30 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.service.impl
 * To change this template use File | Settings | File and Code Template
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionProcessorImpl implements TransactionProcessor {

    /**
     * Instance of the account service to create the details of the account
     */
    private final AccountService accountService;

    /**
     * The transaction repository instance to perform the CRUD operations
     */
    private final TransactionRepository transactionRepository;

    /**
     * The transaction mapper instance for mapping the transaction
     */
    private final TransactionMapper transactionMapper;

    /**
     * Process the transaction received to update/create an account in MMA
     * @param transactionDto
     * @param accountNumber
     */
    @Override
    public void processTransaction(TransactionDto transactionDto, String accountNumber) throws JsonProcessingException {
        Transaction transaction = transactionMapper.transactionDtoToTransaction(transactionDto);
        transaction = transactionRepository.save(transaction);
        if(accountNumber == null){
            // If the account number is null, a new account has to be created in MMS
            accountService.createAccount(transactionDto, transaction);
        }
    }
}
