package com.brihaspathee.zeus.mapper.impl;

import com.brihaspathee.zeus.domain.entity.MemberEmail;
import com.brihaspathee.zeus.dto.account.MemberEmailDto;
import com.brihaspathee.zeus.mapper.interfaces.MemberEmailMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 30, November 2022
 * Time: 4:09 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.mapper.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberEmailMapperImpl implements MemberEmailMapper {

    /**
     * Convert email entity to email dto
     * @param email
     * @return
     */
    @Override
    public MemberEmailDto emailToEmailDto(MemberEmail email) {
        if(email == null){
            return null;
        }
        MemberEmailDto emailDto = MemberEmailDto.builder()
                .memberEmailSK(email.getMemberAcctEmailSK())
                .memberEmailCode(email.getMemberEmailCode())
                .emailTypeCode(email.getEmailTypeCode())
                .email(email.getEmail())
                .isPrimary(email.isPrimary())
                .startDate(email.getStartDate())
                .endDate(email.getEndDate())
                .createdDate(email.getCreatedDate())
                .updatedDate(email.getUpdatedDate())
                .build();
        return emailDto;
    }

    /**
     * Convert email entities to email dtos
     * @param emails
     * @return
     */
    @Override
    public List<MemberEmailDto> emailsToEmailDtos(List<MemberEmail> emails) {
        return emails.stream().map(this::emailToEmailDto).collect(Collectors.toList());
    }
}
