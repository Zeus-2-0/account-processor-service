package com.brihaspathee.zeus.mapper.interfaces;

import com.brihaspathee.zeus.domain.entity.Broker;
import com.brihaspathee.zeus.dto.account.BrokerDto;

import java.util.List;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 30, November 2022
 * Time: 10:44 AM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.mapper.interfaces
 * To change this template use File | Settings | File and Code Template
 */
public interface BrokerMapper {

    /**
     * Convert the broker entity to broker dto
     * @param broker
     * @return
     */
    BrokerDto brokerToBrokerDto(Broker broker);

    /**
     * Convert broker dto to broker entity
     * @param brokerDto
     * @return
     */
    Broker brokerDtoToBroker(BrokerDto brokerDto);

    /**
     * Convert broker entities to broker dtos
     * @param brokers
     * @return
     */
    List<BrokerDto> brokersToBrokerDtos(List<Broker> brokers);

    /**
     * Convert broker dtos to broker entities
     * @param brokerDtos
     * @return
     */
    List<Broker> brokerDtosToBrokers(List<BrokerDto> brokerDtos);
}
