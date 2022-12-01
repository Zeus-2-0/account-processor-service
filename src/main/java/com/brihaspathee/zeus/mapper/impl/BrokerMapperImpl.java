package com.brihaspathee.zeus.mapper.impl;

import com.brihaspathee.zeus.domain.entity.Broker;
import com.brihaspathee.zeus.dto.account.BrokerDto;
import com.brihaspathee.zeus.mapper.interfaces.BrokerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 30, November 2022
 * Time: 10:48 AM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.mapper.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BrokerMapperImpl implements BrokerMapper {

    /**
     * Convert broker entity to broker dto
     * @param broker
     * @return
     */
    @Override
    public BrokerDto brokerToBrokerDto(Broker broker) {
        if(broker == null){
            return null;
        }
        BrokerDto brokerDto = BrokerDto.builder()
                .brokerSK(broker.getAcctBrokerSK())
                .brokerCode(broker.getBrokerCode())
                .brokerId(broker.getBrokerId())
                .brokerName(broker.getBrokerName())
                .agencyId(broker.getAgencyId())
                .agencyName(broker.getAgencyName())
                .accountNumber1(broker.getAccountNumber1())
                .accountNumber2(broker.getAccountNumber2())
                .startDate(broker.getStartDate())
                .endDate(broker.getEndDate())
                .createdDate(broker.getCreatedDate())
                .updatedDate(broker.getUpdatedDate())
                .build();
        return brokerDto;
    }

    /**
     * Convert broker entities to broker dtos
     * @param brokers
     * @return
     */
    @Override
    public List<BrokerDto> brokersToBrokerDtos(List<Broker> brokers) {
        return brokers.stream().map(this::brokerToBrokerDto).collect(Collectors.toList());
    }
}
