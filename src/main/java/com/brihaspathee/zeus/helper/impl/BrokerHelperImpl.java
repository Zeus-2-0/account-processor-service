package com.brihaspathee.zeus.helper.impl;

import com.brihaspathee.zeus.domain.entity.Account;
import com.brihaspathee.zeus.domain.entity.Broker;
import com.brihaspathee.zeus.domain.repository.BrokerRepository;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import com.brihaspathee.zeus.helper.interfaces.BrokerHelper;
import com.brihaspathee.zeus.util.ZeusRandomStringGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 27, November 2022
 * Time: 4:40 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.helper.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BrokerHelperImpl implements BrokerHelper {

    /**
     * Broker repository instance to perform CRUD operations
     */
    private final BrokerRepository brokerRepository;

    /**
     * Create Broker
     * @param transactionDto
     */
    @Override
    public void createBroker(TransactionDto transactionDto, Account account) {
        if(transactionDto.getBroker() != null){
            List<Broker> brokers = new ArrayList<>();
            Broker broker = Broker.builder()
                    .acctBrokerSK(null)
                    .account(account)
                    .brokerCode(ZeusRandomStringGenerator.randomString(15))
                    .brokerName(transactionDto.getBroker().getBrokerName())
                    .brokerId((transactionDto.getBroker().getBrokerId()))
                    .agencyName(transactionDto.getBroker().getAgencyName())
                    .agencyId(transactionDto.getBroker().getAgencyId())
                    .accountNumber1(transactionDto.getBroker().getAccountNumber1())
                    .accountNumber2(transactionDto.getBroker().getAccountNumber2())
                    .startDate(transactionDto.getBroker().getReceivedDate().toLocalDate())
                    .endDate(null)
                    .build();
            broker = brokerRepository.save(broker);
            brokers.add(broker);
            account.setBrokers(brokers);
        }

    }
}
