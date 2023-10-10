package com.brihaspathee.zeus.mapper.interfaces;

import com.brihaspathee.zeus.domain.entity.MemberEmail;
import com.brihaspathee.zeus.dto.account.MemberEmailDto;

import java.util.List;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 30, November 2022
 * Time: 4:07 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.mapper.interfaces
 * To change this template use File | Settings | File and Code Template
 */
public interface MemberEmailMapper {

    /**
     * Convert email entity to email dto
     * @param email
     * @return
     */
    MemberEmailDto emailToEmailDto(MemberEmail email);

    /**
     * Convert email dto to email entity
     * @param emailDto
     * @return
     */
    MemberEmail emailDtoToEmail(MemberEmailDto emailDto);

    /**
     * Convert email entities to email dtos
     * @param emails
     * @return
     */
    List<MemberEmailDto> emailsToEmailDtos(List<MemberEmail> emails);

    /**
     * Convert email dtos to email entities
     * @param emailDtos
     * @return
     */
    List<MemberEmail> emailDtosToEmails(List<MemberEmailDto> emailDtos);
}
