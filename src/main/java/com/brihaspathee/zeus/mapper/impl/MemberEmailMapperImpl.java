package com.brihaspathee.zeus.mapper.impl;

import com.brihaspathee.zeus.domain.entity.MemberEmail;
import com.brihaspathee.zeus.dto.account.MemberEmailDto;
import com.brihaspathee.zeus.mapper.interfaces.MemberEmailMapper;
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
                .ztcn(email.getZtcn())
                .source(email.getSource())
                .startDate(email.getStartDate())
                .endDate(email.getEndDate())
                .changed(new AtomicBoolean(email.isChanged()))
                .createdDate(email.getCreatedDate())
                .updatedDate(email.getUpdatedDate())
                .build();
        return emailDto;
    }

    /**
     * Convert email dto to email entity
     * @param emailDto
     * @return
     */
    @Override
    public MemberEmail emailDtoToEmail(MemberEmailDto emailDto) {
        if(emailDto == null){
            return null;
        }
        MemberEmail email = MemberEmail.builder()
                .memberEmailCode(emailDto.getMemberEmailCode())
                .emailTypeCode(emailDto.getEmailTypeCode())
                .email(emailDto.getEmail())
                .isPrimary(emailDto.isPrimary())
                .ztcn(emailDto.getZtcn())
                .source(emailDto.getSource())
                .startDate(emailDto.getStartDate())
                .endDate(emailDto.getEndDate())
                .createdDate(emailDto.getCreatedDate())
                .updatedDate(emailDto.getUpdatedDate())
                .build();
        if (emailDto.getChanged() != null){
            email.setChanged(emailDto.getChanged().get());
        } else {
            email.setChanged(false);
        }
        return email;
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

    /**
     * Convert email dtos to email entities
     * @param emailDtos
     * @return
     */
    @Override
    public List<MemberEmail> emailDtosToEmails(List<MemberEmailDto> emailDtos) {
        return emailDtos.stream().map(this::emailDtoToEmail).collect(Collectors.toList());
    }
}
