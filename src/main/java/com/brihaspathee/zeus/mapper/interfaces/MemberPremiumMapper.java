package com.brihaspathee.zeus.mapper.interfaces;

import com.brihaspathee.zeus.domain.entity.MemberPremium;
import com.brihaspathee.zeus.dto.account.MemberPremiumDto;

import java.util.List;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 01, December 2022
 * Time: 1:22 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.mapper.interfaces
 * To change this template use File | Settings | File and Code Template
 */
public interface MemberPremiumMapper {

    /**
     * Member premium entity to member premium dto
     * @param memberPremium
     * @return
     */
    MemberPremiumDto memberPremiumToMemberPremiumDto(MemberPremium memberPremium);

    /**
     * Convert member premium entities to member premium dtos
     * @param memberPremiums
     * @return
     */
    List<MemberPremiumDto> memberPremiumsToMemberPremiumDtos(List<MemberPremium> memberPremiums);

}
