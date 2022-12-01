package com.brihaspathee.zeus.helper.impl;

import com.brihaspathee.zeus.domain.entity.Member;
import com.brihaspathee.zeus.domain.entity.MemberPhone;
import com.brihaspathee.zeus.domain.repository.MemberPhoneRepository;
import com.brihaspathee.zeus.dto.account.MemberDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberDto;
import com.brihaspathee.zeus.helper.interfaces.MemberPhoneHelper;
import com.brihaspathee.zeus.mapper.interfaces.MemberPhoneMapper;
import com.brihaspathee.zeus.util.ZeusRandomStringGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 27, November 2022
 * Time: 7:04 AM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.helper.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberPhoneHelperImpl implements MemberPhoneHelper {

    /**
     * Phone mapper instance
     */
    private final MemberPhoneMapper phoneMapper;

    /**
     * Member phone repository to perform CRUD operations
     */
    private final MemberPhoneRepository memberPhoneRepository;

    /**
     * Create member phone
     * @param member
     * @param transactionMemberDto
     */
    @Override
    public void createMemberPhone(Member member, TransactionMemberDto transactionMemberDto) {
        if(transactionMemberDto.getMemberPhones() != null && transactionMemberDto.getMemberPhones().size() > 0){
            List<MemberPhone> phones = new ArrayList<>();
            transactionMemberDto.getMemberPhones().stream().forEach(phoneDto -> {
                MemberPhone memberPhone = MemberPhone.builder()
                        .member(member)
                        .memberAcctPhoneSK(null)
                        .memberPhoneCode(ZeusRandomStringGenerator.randomString(15))
                        .phoneTypeCode(phoneDto.getPhoneTypeCode())
                        .phoneNumber(phoneDto.getPhoneNumber())
                        .startDate(phoneDto.getReceivedDate().toLocalDate())
                        .endDate(null)
                        .build();
                memberPhone = memberPhoneRepository.save(memberPhone);
                phones.add(memberPhone);
            });
            member.setMemberPhones(phones);
        }
    }

    /**
     * Set the member phone dto to  send to MMS
     * @param memberDto
     * @param member
     */
    @Override
    public void setMemberPhone(MemberDto memberDto, Member member) {
        if(member.getMemberPhones() != null && member.getMemberPhones().size() >0){
            memberDto.setMemberPhones(
                    phoneMapper
                            .phonesToPhoneDtos(
                                    member.getMemberPhones())
                            .stream()
                            .collect(Collectors.toSet())
            );
        }
    }
}
