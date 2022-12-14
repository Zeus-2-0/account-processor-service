package com.brihaspathee.zeus.mapper.impl;

import com.brihaspathee.zeus.domain.entity.AlternateContact;
import com.brihaspathee.zeus.dto.account.AlternateContactDto;
import com.brihaspathee.zeus.mapper.interfaces.AlternateContactMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 30, November 2022
 * Time: 1:40 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.mapper.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlternateContactMapperImpl implements AlternateContactMapper {

    /**
     * Convert alternate contact entity to alternate contact dto
     * @param alternateContact
     * @return
     */
    @Override
    public AlternateContactDto alternateContactToAlternateContactDto(AlternateContact alternateContact) {
        if(alternateContact == null){
            return null;
        }
        AlternateContactDto alternateContactDto = AlternateContactDto.builder()
                .alternateContactSK(alternateContact.getAcctAltContactSK())
                .alternateContactCode(alternateContact.getAlternateContactCode())
                .alternateContactTypeCode(alternateContact.getAlternateContactTypeCode())
                .firstName(alternateContact.getFirstName())
                .middleName(alternateContact.getMiddleName())
                .lastName(alternateContact.getLastName())
                .identifierTypeCode(alternateContact.getIdentifierTypeCode())
                .identifierValue(alternateContact.getIdentifierValue())
                .phoneTypeCode(alternateContact.getPhoneTypeCode())
                .phoneNumber(alternateContact.getPhoneNumber())
                .email(alternateContact.getEmail())
                .addressLine1(alternateContact.getAddressLine1())
                .addressLine2(alternateContact.getAddressLine2())
                .city(alternateContact.getCity())
                .stateTypeCode(alternateContact.getStateTypeCode())
                .zipCode(alternateContact.getZipCode())
                .startDate(alternateContact.getStartDate())
                .endDate(alternateContact.getEndDate())
                .createdDate(alternateContact.getCreatedDate())
                .updatedDate(alternateContact.getUpdatedDate())
                .build();
        return alternateContactDto;
    }

    /**
     * Convert alternate contact entities to alternate contact dtos
     * @param alternateContacts
     * @return
     */
    @Override
    public List<AlternateContactDto> alternateContactsToAlternateContactDtos(List<AlternateContact> alternateContacts) {
        return alternateContacts.stream().map(this::alternateContactToAlternateContactDto).collect(Collectors.toList());
    }
}
