package com.brihaspathee.zeus.mapper.interfaces;

import com.brihaspathee.zeus.domain.entity.Member;
import com.brihaspathee.zeus.dto.account.MemberDto;

import java.util.List;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 30, November 2022
 * Time: 12:33 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.mapper.interfaces
 * To change this template use File | Settings | File and Code Template
 */
public interface MemberMapper {

    /**
     * Convert member entity to member dto
     * @param member
     * @return
     */
    MemberDto memberToMemberDto(Member member);

    /**
     * Convert member dto to member entity
     * @param memberDto
     * @return
     */
    Member memberDtoToMember(MemberDto memberDto);

    /**
     * Convert the member entities to member dtos
     * @param members
     * @return
     */
    List<MemberDto> membersToMemberDtos(List<Member> members);

    /**
     * Convert member dtos to member entities
     * @param memberDtos
     * @return
     */
    List<Member> memberDtosToMembers(List<MemberDto> memberDtos);
}
