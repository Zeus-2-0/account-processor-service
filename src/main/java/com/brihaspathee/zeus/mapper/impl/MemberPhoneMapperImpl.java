package com.brihaspathee.zeus.mapper.impl;

import com.brihaspathee.zeus.domain.entity.MemberPhone;
import com.brihaspathee.zeus.dto.account.MemberPhoneDto;
import com.brihaspathee.zeus.mapper.interfaces.MemberPhoneMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 30, November 2022
 * Time: 3:34 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.mapper.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberPhoneMapperImpl implements MemberPhoneMapper {

    /**
     * Convert Phone entity to phone dto
     * @param phone
     * @return
     */
    @Override
    public MemberPhoneDto phoneToPhoneDto(MemberPhone phone) {
        if(phone == null){
            return null;
        }
        MemberPhoneDto phoneDto = MemberPhoneDto.builder()
                .memberPhoneSK(phone.getMemberAcctPhoneSK())
                .memberPhoneCode(phone.getMemberPhoneCode())
                .phoneTypeCode(phone.getPhoneTypeCode())
                .phoneNumber(phone.getPhoneNumber())
                .startDate(phone.getStartDate())
                .endDate(phone.getEndDate())
                .createdDate(phone.getCreatedDate())
                .updatedDate(phone.getUpdatedDate())
                .build();
        return phoneDto;
    }

    /**
     * Convert phone entities to phone dtos
     * @param phones
     * @return
     */
    @Override
    public List<MemberPhoneDto> phonesToPhoneDtos(List<MemberPhone> phones) {
        return phones.stream().map(this::phoneToPhoneDto).collect(Collectors.toList());
    }
}
