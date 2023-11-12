package com.brihaspathee.zeus.mapper.impl;

import com.brihaspathee.zeus.domain.entity.MemberAddress;
import com.brihaspathee.zeus.dto.account.MemberAddressDto;
import com.brihaspathee.zeus.mapper.interfaces.MemberAddressMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
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
                .changed(new AtomicBoolean(memberAddress.isChanged()))
                .createdDate(memberAddress.getCreatedDate())
                .updatedDate(memberAddress.getUpdatedDate())
                .build();
        return memberAddressDto;
    }

    /**
     * Convert member address dto to member address entity
     * @param memberAddressDto
     * @return
     */
    @Override
    public MemberAddress memberAddressDtoToMemberAddress(MemberAddressDto memberAddressDto) {
        if(memberAddressDto == null){
            return null;
        }
        MemberAddress memberAddress = MemberAddress.builder()
                .memberAddressCode(memberAddressDto.getMemberAddressCode())
                .addressTypeCode(memberAddressDto.getAddressTypeCode())
                .addressLine1(memberAddressDto.getAddressLine1())
                .addressLine2(memberAddressDto.getAddressLine2())
                .city(memberAddressDto.getCity())
                .stateTypeCode(memberAddressDto.getStateTypeCode())
                .zipCode(memberAddressDto.getZipCode())
                .startDate(memberAddressDto.getStartDate())
                .endDate(memberAddressDto.getEndDate())
                .createdDate(memberAddressDto.getCreatedDate())
                .updatedDate(memberAddressDto.getUpdatedDate())
                .build();
        if(memberAddressDto.getChanged() != null){
            memberAddress.setChanged(memberAddressDto.getChanged().get());
        }else {
            memberAddress.setChanged(false);
        }
        return memberAddress;
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

    /**
     * Convert member address dtos to member address entities
     * @param memberAddressDtos
     * @return
     */
    @Override
    public List<MemberAddress> memberAddressDtosToMemberAddresses(List<MemberAddressDto> memberAddressDtos) {
        return memberAddressDtos.stream().map(this::memberAddressDtoToMemberAddress).collect(Collectors.toList());
    }
}
