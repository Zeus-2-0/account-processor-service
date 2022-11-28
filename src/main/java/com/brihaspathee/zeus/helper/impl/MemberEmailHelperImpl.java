package com.brihaspathee.zeus.helper.impl;

import com.brihaspathee.zeus.domain.entity.Member;
import com.brihaspathee.zeus.domain.entity.MemberEmail;
import com.brihaspathee.zeus.domain.repository.MemberEmailRepository;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberDto;
import com.brihaspathee.zeus.helper.interfaces.MemberEmailHelper;
import com.brihaspathee.zeus.util.ZeusRandomStringGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 26, November 2022
 * Time: 7:15 AM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.helper.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberEmailHelperImpl implements MemberEmailHelper {

    /**
     * Member email repository instance to perform CRUD operations
     */
    private final MemberEmailRepository memberEmailRepository;

    /**
     * Create the member email
     * @param member
     * @param transactionMemberDto
     */
    @Override
    public void createMemberEmail(Member member, TransactionMemberDto transactionMemberDto) {
        if(transactionMemberDto.getEmails() != null && transactionMemberDto.getEmails().size() > 0){
            List<MemberEmail> emails = new ArrayList<>();
            transactionMemberDto.getEmails().stream().forEach(emailDto -> {
                MemberEmail memberEmail = MemberEmail.builder()
                        .member(member)
                        .memberAcctEmailSK(null)
                        .memberEmailCode(ZeusRandomStringGenerator.randomString(15))
                        .emailTypeCode("PERSONAL")
                        .email(emailDto.getEmail())
                        .isPrimary(true)
                        .startDate(emailDto.getReceivedDate().toLocalDate())
                        .endDate(null)
                        .build();
                memberEmail = memberEmailRepository.save(memberEmail);
                emails.add(memberEmail);
            });
            member.setMemberEmails(emails);
        }
    }
}
