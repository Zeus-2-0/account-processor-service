package com.brihaspathee.zeus.helper.impl;

import com.brihaspathee.zeus.domain.entity.Account;
import com.brihaspathee.zeus.domain.entity.Member;
import com.brihaspathee.zeus.domain.repository.MemberRepository;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.account.MemberDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberDto;
import com.brihaspathee.zeus.helper.interfaces.*;
import com.brihaspathee.zeus.mapper.interfaces.MemberMapper;
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
 * Date: 23, November 2022
 * Time: 7:21 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.helper.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberHelperImpl implements MemberHelper {

    /**
     * Member repository instance to perform CRUD operations
     */
    private final MemberRepository memberRepository;

    /**
     * Member mapper instance
     */
    private final MemberMapper memberMapper;

    /**
     * Member email helper instance
     */
    private final MemberEmailHelper memberEmailHelper;

    /**
     * Member address helper instance
     */
    private final MemberAddressHelper memberAddressHelper;

    /**
     * Member language helper instance
     */
    private final MemberLanguageHelper memberLanguageHelper;

    /**
     * Member identifier helper instance
     */
    private final MemberIdentifierHelper memberIdentifierHelper;

    /**
     * Member phone helper instance
     */
    private final MemberPhoneHelper memberPhoneHelper;

    /**
     * Alternate contact helper instance
     */
    private final AlternateContactHelper alternateContactHelper;

    /**
     * Create member
     * @param members
     * @param account
     */
    @Override
    public List<Member> createMember(List<TransactionMemberDto> members,
                             Account account) {
        List<Member> savedMembers = new ArrayList<>();
        members.stream().forEach(memberDto -> {
            log.info("member detail:{}", memberDto);
            Member member = Member.builder()
                    .account(account)
                    // New member is being created so this will be NULL
                    .acctMemberSK(null)
                    // The member code assigned for the member in the transaction manager service
                    .transactionMemberCode(memberDto.getTransactionMemberCode())
                    // New member code that is created to send to MMS
                    .memberCode(ZeusRandomStringGenerator.randomString(15))
                    .relationShipTypeCode(memberDto.getRelationshipTypeCode())
                    .firstName(memberDto.getFirstName())
                    .middleName(memberDto.getMiddleName())
                    .lastName(memberDto.getLastName())
                    .dateOfBirth(memberDto.getDateOfBirth())
                    .genderTypeCode(memberDto.getGenderTypeCode())
                    .tobaccoInd(false)
                    .build();
            member = memberRepository.save(member);
            memberAddressHelper.createMemberAddress(member, memberDto);
            memberEmailHelper.createMemberEmail(member, memberDto);
            memberPhoneHelper.createMemberPhone(member, memberDto);
            memberLanguageHelper.createMemberLanguage(member, memberDto);
            memberIdentifierHelper.createMemberIdentifier(member, memberDto);
            alternateContactHelper.createAlternateContact(member, memberDto);
            savedMembers.add(member);
        });
        return savedMembers;
    }

    /**
     * Set the member information in the account to send to MMS
     * @param accountDto
     * @param account
     */
    @Override
    public void setMember(AccountDto accountDto, Account account) {
        if(account.getMembers() != null && account.getSponsors().size() > 0){
            List<MemberDto> memberDtos = new ArrayList<>();
            account.getMembers().stream().forEach(member -> {
                MemberDto memberDto = memberMapper.memberToMemberDto(member);
                alternateContactHelper.setAlternateContact(memberDto, member);
                memberAddressHelper.setMemberAddress(memberDto, member);
                memberPhoneHelper.setMemberPhone(memberDto, member);
                memberLanguageHelper.setMemberLanguage(memberDto, member);
                memberEmailHelper.setMemberEmail(memberDto, member);
                memberIdentifierHelper.setMemberIdentifier(memberDto,member);
                memberDtos.add(memberDto);
            });
            accountDto.setMembers(memberDtos.stream().collect(Collectors.toSet()));
        }
    }
}
