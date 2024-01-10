package com.brihaspathee.zeus.mapper.impl;

import com.brihaspathee.zeus.domain.entity.Member;
import com.brihaspathee.zeus.dto.account.MemberDto;
import com.brihaspathee.zeus.mapper.interfaces.MemberMapper;
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
                .height(member.getHeight())
                .weight(member.getWeight())
                .ztcn(member.getZtcn())
                .source(member.getSource())
                .changed(new AtomicBoolean(member.isChanged()))
                .createdDate(member.getCreatedDate())
                .updatedDate(member.getUpdatedDate())
                .build();
        return memberDto;
    }

    /**
     * Convert member dto to member entity
     * @param memberDto
     * @return
     */
    @Override
    public Member memberDtoToMember(MemberDto memberDto) {
        if(memberDto == null){
            return null;
        }
        Member member = Member.builder()
                .memberSK(memberDto.getMemberSK())
                .memberCode(memberDto.getMemberCode())
                .firstName(memberDto.getFirstName())
                .lastName(memberDto.getLastName())
                .middleName(memberDto.getMiddleName())
                .relationShipTypeCode(memberDto.getRelationshipTypeCode())
                .dateOfBirth(memberDto.getDateOfBirth())
                .genderTypeCode(memberDto.getGenderTypeCode())
                .height(memberDto.getHeight())
                .weight(memberDto.getWeight())
                .ztcn(memberDto.getZtcn())
                .source(memberDto.getSource())
                .createdDate(memberDto.getCreatedDate())
                .updatedDate(memberDto.getUpdatedDate())
                .build();
        if (memberDto.getChanged() != null){
            member.setChanged(memberDto.getChanged().get());
        } else {
            member.setChanged(false);
        }
        return member;
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

    /**
     * Convert member dtos to member entities
     * @param memberDtos
     * @return
     */
    @Override
    public List<Member> memberDtosToMembers(List<MemberDto> memberDtos) {
        return memberDtos.stream().map(this::memberDtoToMember).collect(Collectors.toList());
    }
}
