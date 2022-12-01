package com.brihaspathee.zeus.mapper.interfaces;

import com.brihaspathee.zeus.domain.entity.MemberPhone;
import com.brihaspathee.zeus.dto.account.MemberPhoneDto;

import java.util.List;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 30, November 2022
 * Time: 3:27 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.mapper.interfaces
 * To change this template use File | Settings | File and Code Template
 */
public interface MemberPhoneMapper {

    /**
     * Convert phone entity to phone dto
     * @param phone
     * @return
     */
    MemberPhoneDto phoneToPhoneDto(MemberPhone phone);

    /**
     * Convert phone entities to phone dtos
     * @param phones
     * @return
     */
    List<MemberPhoneDto> phonesToPhoneDtos(List<MemberPhone> phones);
}
