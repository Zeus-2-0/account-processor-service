package com.brihaspathee.zeus.mapper.interfaces;

import com.brihaspathee.zeus.domain.entity.Payer;
import com.brihaspathee.zeus.dto.account.PayerDto;

import java.util.List;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 30, November 2022
 * Time: 11:34 AM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.mapper.interfaces
 * To change this template use File | Settings | File and Code Template
 */
public interface PayerMapper {

    /**
     * Convert Payer dto to payer entity
     * @param payer
     * @return
     */
    PayerDto payerToPayerDto(Payer payer);

    /**
     * Convert payer dto to payer entity
     * @param payerDto
     * @return
     */
    Payer payerDtoToPayer(PayerDto payerDto);

    /**
     * Convert payer entities to payer dtos
     * @param payers
     * @return
     */
    List<PayerDto> payersToPayerDtos(List<Payer> payers);

    /**
     * Convert payer dtos to payer entities
     * @param payerDtos
     * @return
     */
    List<Payer> payerDtosToPayers(List<PayerDto> payerDtos);
}
