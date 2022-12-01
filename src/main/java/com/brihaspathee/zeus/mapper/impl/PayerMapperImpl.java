package com.brihaspathee.zeus.mapper.impl;

import com.brihaspathee.zeus.domain.entity.Payer;
import com.brihaspathee.zeus.dto.account.PayerDto;
import com.brihaspathee.zeus.mapper.interfaces.PayerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 30, November 2022
 * Time: 11:36 AM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.mapper.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayerMapperImpl implements PayerMapper {

    /**
     * Convert payer dto to payer entity
     * @param payer
     * @return
     */
    @Override
    public PayerDto payerToPayerDto(Payer payer) {
        if(payer == null){
            return null;
        }
        PayerDto payerDto = PayerDto.builder()
                .payerSK(payer.getAcctPayerSK())
                .payerCode(payer.getPayerCode())
                .payerId(payer.getPayerId())
                .payerName(payer.getPayerName())
                .startDate(payer.getStartDate())
                .endDate(payer.getEndDate())
                .createdDate(payer.getCreatedDate())
                .updatedDate(payer.getUpdatedDate())
                .build();
        return payerDto;
    }

    /**
     * Conver payer entities to payer dtos
     * @param payers
     * @return
     */
    @Override
    public List<PayerDto> payersToPayerDtos(List<Payer> payers) {
        return payers.stream().map(this::payerToPayerDto).collect(Collectors.toList());
    }
}
