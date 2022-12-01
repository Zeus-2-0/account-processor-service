package com.brihaspathee.zeus.mapper.interfaces;

import com.brihaspathee.zeus.domain.entity.MemberAddress;
import com.brihaspathee.zeus.dto.account.MemberAddressDto;

import java.util.List;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 30, November 2022
 * Time: 1:11 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.mapper.interfaces
 * To change this template use File | Settings | File and Code Template
 */
public interface MemberAddressMapper {

    /**
     * Convert member address to member address dto
     * @param memberAddress
     * @return
     */
    MemberAddressDto memberAddressToMemberAddressDto(MemberAddress memberAddress);

    /**
     * Convert member address entities to member address dtos
     * @param memberAddresses
     * @return
     */
    List<MemberAddressDto> memberAddressesToMemberAddressDtos(List<MemberAddress> memberAddresses);
}
