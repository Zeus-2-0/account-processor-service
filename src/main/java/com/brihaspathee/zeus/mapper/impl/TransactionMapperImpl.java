package com.brihaspathee.zeus.mapper.impl;

import com.brihaspathee.zeus.domain.entity.Transaction;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import com.brihaspathee.zeus.mapper.interfaces.TransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 23, November 2022
 * Time: 1:44 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.mapper.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionMapperImpl implements TransactionMapper {

    /**
     * Convert transaction dto to transaction entity
     * @param transactionDto
     * @return
     */
    @Override
    public Transaction transactionDtoToTransaction(TransactionDto transactionDto) {
        if(transactionDto == null){
            return null;
        }
        Transaction transaction = Transaction.builder()
                .ztcn(transactionDto.getZtcn())
                .zfcn(transactionDto.getZfcn())
                .source(transactionDto.getSource())
                .transactionReceivedDate(transactionDto.getTransactionReceivedDate())
                .build();
        return transaction;
    }
}
