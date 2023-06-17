package com.brihaspathee.zeus.helper.impl;

import com.brihaspathee.zeus.domain.entity.Member;
import com.brihaspathee.zeus.domain.entity.MemberIdentifier;
import com.brihaspathee.zeus.domain.repository.MemberIdentifierRepository;
import com.brihaspathee.zeus.dto.account.MemberDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberIdentifierDto;
import com.brihaspathee.zeus.helper.interfaces.MemberIdentifierHelper;
import com.brihaspathee.zeus.mapper.interfaces.MemberIdentifierMapper;
import com.brihaspathee.zeus.util.AccountProcessorUtil;
import com.brihaspathee.zeus.util.ZeusRandomStringGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 27, November 2022
 * Time: 7:03 AM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.helper.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberIdentifierHelperImpl implements MemberIdentifierHelper {

    /**
     * Member identifier mapper instance
     */
    private final MemberIdentifierMapper identifierMapper;

    /**
     * Member identifier repository to perform CRUD operations
     */
    private final MemberIdentifierRepository memberIdentifierRepository;

    /**
     * The utility class for account processor service
     */
    private final AccountProcessorUtil accountProcessorUtil;

    @Override
    public void createMemberIdentifier(Member member, TransactionMemberDto transactionMemberDto) {
        if(transactionMemberDto.getIdentifiers() != null && transactionMemberDto.getIdentifiers().size() > 0){
            List<MemberIdentifier> identifiers = new ArrayList<>();
            List<TransactionMemberIdentifierDto> memberIdentifierDtos = transactionMemberDto.getIdentifiers().stream().filter(memberIdentifierDto -> {
                return !memberIdentifierDto.getIdentifierTypeCode().equals("EXCHSUBID") ||
                        !memberIdentifierDto.getIdentifierTypeCode().equals("EXCHMEMID");
            }).collect(Collectors.toList());
            memberIdentifierDtos.stream().forEach(memberIdentifierDto -> {
                String memberIdentifierCode = accountProcessorUtil.generateUniqueCode(transactionMemberDto.getEntityCodes(),
                        "memberIdentifierCode");
                MemberIdentifier memberIdentifier = MemberIdentifier.builder()
                        .member(member)
                        .memberAcctIdentifierSK(null)
                        .memberIdentifierCode(memberIdentifierCode)
                        .identifierTypeCode(memberIdentifierDto.getIdentifierTypeCode())
                        .identifierValue(memberIdentifierDto.getIdentifierValue())
                        .active(true)
                        .changed(true)
                        .build();
                memberIdentifier = memberIdentifierRepository.save(memberIdentifier);
                identifiers.add(memberIdentifier);
            });

            member.setMemberIdentifiers(identifiers);
        }
    }

    /**
     * Set the member identifier dto to  send to MMS
     * @param memberDto
     * @param member
     */
    @Override
    public void setMemberIdentifier(MemberDto memberDto, Member member) {
        if(member.getMemberIdentifiers() != null && member.getMemberIdentifiers().size() >0){
            memberDto.setMemberIdentifiers(
                    identifierMapper
                            .identifiersToIdentifierDtos(
                                    member.getMemberIdentifiers())
                            .stream()
                            .collect(Collectors.toSet())
            );
        }
    }
}
