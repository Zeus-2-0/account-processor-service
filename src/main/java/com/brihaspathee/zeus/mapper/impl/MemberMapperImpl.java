package com.brihaspathee.zeus.mapper.impl;

import com.brihaspathee.zeus.domain.entity.Member;
import com.brihaspathee.zeus.dto.account.MemberDto;
import com.brihaspathee.zeus.mapper.interfaces.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 30, November 2022
 * Time: 12:35 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.mapper.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberMapperImpl implements MemberMapper {

    /**
     * Convert the member entity to member dto
     * @param member
     * @return
     */
    @Override
    public MemberDto memberToMemberDto(Member member) {
        if(member == null){
            return null;
        }
        MemberDto memberDto = MemberDto.builder()
                .memberSK(member.getAcctMemberSK())
                .memberCode(member.getMemberCode())
                .firstName(member.getFirstName())
                .lastName(member.getLastName())
                .middleName(member.getMiddleName())
                .relationshipTypeCode(member.getRelationShipTypeCode())
                .dateOfBirth(member.getDateOfBirth())
                .genderTypeCode(member.getGenderTypeCode())
                .createdDate(member.getCreatedDate())
                .updatedDate(member.getUpdatedDate())
                .build();
        return memberDto;
    }

    /**
     * Convert the member entities to member dtos
     * @param members
     * @return
     */
    @Override
    public List<MemberDto> membersToMemberDtos(List<Member> members) {
        return members.stream().map(this::memberToMemberDto).collect(Collectors.toList());
    }
}
