package com.brihaspathee.zeus.helper.impl;

import com.brihaspathee.zeus.domain.entity.Account;
import com.brihaspathee.zeus.domain.entity.Broker;
import com.brihaspathee.zeus.domain.repository.BrokerRepository;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.account.BrokerDto;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import com.brihaspathee.zeus.helper.interfaces.BrokerHelper;
import com.brihaspathee.zeus.mapper.interfaces.BrokerMapper;
import com.brihaspathee.zeus.util.AccountProcessorUtil;
import com.brihaspathee.zeus.util.ZeusRandomStringGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
     * The broker mapper instance
     */
    private final BrokerMapper brokerMapper;

    /**
     * Broker repository instance to perform CRUD operations
     */
    private final BrokerRepository brokerRepository;

    /**
     * The utility class for account processor service
     */
    private final AccountProcessorUtil accountProcessorUtil;

    /**
     * Create Broker
     * @param transactionDto
     */
    @Override
    public void createBroker(TransactionDto transactionDto, Account account) {
        if(transactionDto.getBroker() != null){
            String brokerCode = accountProcessorUtil.generateUniqueCode(transactionDto.getEntityCodes(),
                    "brokerCode");
            List<Broker> brokers = new ArrayList<>();
            Broker broker = Broker.builder()
                    .acctBrokerSK(null)
                    .account(account)
                    .brokerCode(brokerCode)
                    .brokerName(transactionDto.getBroker().getBrokerName())
                    .brokerId((transactionDto.getBroker().getBrokerId()))
                    .agencyName(transactionDto.getBroker().getAgencyName())
                    .agencyId(transactionDto.getBroker().getAgencyId())
                    .accountNumber1(transactionDto.getBroker().getAccountNumber1())
                    .accountNumber2(transactionDto.getBroker().getAccountNumber2())
                    .ztcn(transactionDto.getZtcn())
                    .source(transactionDto.getSource())
                    .startDate(transactionDto.getBroker().getReceivedDate().toLocalDate())
                    .endDate(null)
                    .changed(true)
                    .build();
            broker = brokerRepository.save(broker);
            brokers.add(broker);
            account.setBrokers(brokers);
        }
    }

    /**
     * Set the broker in the account dto to send to MMS
     * @param accountDto
     * @param account
     */
    @Override
    public void setBroker(AccountDto accountDto, Account account) {
        if(account.getBrokers() != null && account.getBrokers().size() > 0){
            accountDto.setBrokers(brokerMapper
                    .brokersToBrokerDtos(account.getBrokers())
                    .stream()
                    .collect(Collectors.toSet()));
        }
    }
}
