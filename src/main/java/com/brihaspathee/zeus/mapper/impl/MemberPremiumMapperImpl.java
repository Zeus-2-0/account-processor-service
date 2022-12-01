package com.brihaspathee.zeus.mapper.impl;

import com.brihaspathee.zeus.domain.entity.MemberPremium;
import com.brihaspathee.zeus.dto.account.MemberPremiumDto;
import com.brihaspathee.zeus.mapper.interfaces.MemberPremiumMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 01, December 2022
 * Time: 1:23 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.mapper.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberPremiumMapperImpl implements MemberPremiumMapper {

    /**
     * Convert member premium entity to member premium dto
     * @param memberPremium
     * @return
     */
    @Override
    public MemberPremiumDto memberPremiumToMemberPremiumDto(MemberPremium memberPremium) {
        if(memberPremium == null){
            return null;
        }
        MemberPremiumDto memberPremiumDto = MemberPremiumDto.builder()
                .memberPremiumSK(memberPremium.getAcctMemPremSK())
                .memberPremiumSK(memberPremium.getAcctPremSpanSK())
                .memberSK(memberPremium.getAcctMemberSK())
                .memberCode(memberPremium.getMember().getMemberCode())
                .exchangeMemberId(memberPremium.getExchangeMemberId())
                .individualPremiumAmount(memberPremium.getIndividualRateAmount())
                .createdDate(memberPremium.getCreatedDate())
                .updatedDate(memberPremium.getUpdatedDate())
                .build();
        return memberPremiumDto;
    }

    /**
     * Convert member premium entities to member premium dtos
     * @param memberPremiums
     * @return
     */
    @Override
    public List<MemberPremiumDto> memberPremiumsToMemberPremiumDtos(List<MemberPremium> memberPremiums) {
        return memberPremiums.stream().map(this::memberPremiumToMemberPremiumDto).collect(Collectors.toList());
    }
}
