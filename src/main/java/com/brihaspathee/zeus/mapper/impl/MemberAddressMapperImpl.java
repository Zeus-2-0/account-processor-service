package com.brihaspathee.zeus.mapper.impl;

import com.brihaspathee.zeus.domain.entity.MemberAddress;
import com.brihaspathee.zeus.dto.account.MemberAddressDto;
import com.brihaspathee.zeus.mapper.interfaces.MemberAddressMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 30, November 2022
 * Time: 1:14 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.mapper.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberAddressMapperImpl implements MemberAddressMapper {

    /**
     * Convert member address entity to member address dto
     * @param memberAddress
     * @return
     */
    @Override
    public MemberAddressDto memberAddressToMemberAddressDto(MemberAddress memberAddress) {
        if(memberAddress == null){
            return null;
        }
        MemberAddressDto memberAddressDto = MemberAddressDto.builder()
                .memberAddressSK(memberAddress.getMemberAcctAddressSK())
                .memberAddressCode(memberAddress.getMemberAddressCode())
                .addressTypeCode(memberAddress.getAddressTypeCode())
                .addressLine1(memberAddress.getAddressLine1())
                .addressLine2(memberAddress.getAddressLine2())
                .city(memberAddress.getCity())
                .stateTypeCode(memberAddress.getStateTypeCode())
                .zipCode(memberAddress.getZipCode())
                .startDate(memberAddress.getStartDate())
                .endDate(memberAddress.getEndDate())
                .createdDate(memberAddress.getCreatedDate())
                .updatedDate(memberAddress.getUpdatedDate())
                .build();
        return memberAddressDto;
    }

    /**
     * Convert member address entities to member address dtos
     * @param memberAddresses
     * @return
     */
    @Override
    public List<MemberAddressDto> memberAddressesToMemberAddressDtos(List<MemberAddress> memberAddresses) {
        return memberAddresses.stream().map(this::memberAddressToMemberAddressDto).collect(Collectors.toList());
    }
}
