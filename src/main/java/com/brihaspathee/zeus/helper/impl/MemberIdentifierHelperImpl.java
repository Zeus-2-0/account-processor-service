package com.brihaspathee.zeus.helper.impl;

import com.brihaspathee.zeus.domain.entity.Member;
import com.brihaspathee.zeus.domain.entity.MemberIdentifier;
import com.brihaspathee.zeus.domain.repository.MemberIdentifierRepository;
import com.brihaspathee.zeus.dto.account.MemberDto;
import com.brihaspathee.zeus.dto.account.MemberIdentifierDto;
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
import java.util.Optional;
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
        log.info("Inside create member identifier");
        if(transactionMemberDto.getIdentifiers() != null && transactionMemberDto.getIdentifiers().size() > 0){
            List<MemberIdentifier> identifiers = new ArrayList<>();
            // Retrieve all identifiers of the member that are not "Exchange subscriber id and Exchange Member Id"
            List<TransactionMemberIdentifierDto> memberIdentifierDtos = transactionMemberDto.getIdentifiers().stream().filter(memberIdentifierDto -> {
                log.info("Identifier Value:{}", memberIdentifierDto.getIdentifierValue());
                log.info("Identifier Type Code:{}", memberIdentifierDto.getIdentifierTypeCode());
                return !memberIdentifierDto.getIdentifierTypeCode().equals("EXCHSUBID") &&
                        !memberIdentifierDto.getIdentifierTypeCode().equals("EXCHMEMID");
            }).collect(Collectors.toList());
            log.info("Member identifiers size:{}", memberIdentifierDtos.size());
            memberIdentifierDtos.stream().forEach(memberIdentifierDto -> {
                String memberIdentifierCode = accountProcessorUtil.generateUniqueCode(transactionMemberDto.getEntityCodes(),
                        "memberIdentifierCode");
                log.info("Member Identifier Code created:{}", memberIdentifierCode);
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

    /**
     * Match the SSN of the member from the transaction to see if they match with the SSN
     * the member has in the account
     * @param member - The member entity
     * @param memberDto - The member dto
     * @param transactionMemberDto - The member's transaction information
     */
    @Override
    public void matchMemberIdentifier(Member member, MemberDto memberDto, TransactionMemberDto transactionMemberDto) {
        log.info("Inside member match identifier");
        // Check if the transaction has any addresses for the member
        // If there are no addresses in the transaction then return

        if(transactionMemberDto.getIdentifiers() == null ||
            transactionMemberDto.getIdentifiers().isEmpty()){
            return;
        }
        List<MemberIdentifier> identifiers = new ArrayList<>();
        // Check if SSN is present in the transaction for the member
        Optional<TransactionMemberIdentifierDto> optionalTransactionSSN = transactionMemberDto.getIdentifiers().stream()
                .filter(
                        transactionMemberIdentifierDto ->
                                transactionMemberIdentifierDto.getIdentifierTypeCode().equals("SSN")
                ).findFirst();
        log.info("Does the transaction have SSN:{}", optionalTransactionSSN.isEmpty());
        if(optionalTransactionSSN.isEmpty()){
            return;
        }
        // If the control reaches here, it means that the transaction contains an active
        // SSN for the member
        String memberTransactionSSN = optionalTransactionSSN.get().getIdentifierValue();
        log.info("Transaction SSN:{}", memberTransactionSSN);
        Optional<MemberIdentifierDto> optionalAccountSSN = memberDto.getMemberIdentifiers().stream()
                .filter(
                        memberIdentifierDto -> memberIdentifierDto.getIdentifierTypeCode().equals("SSN") &&
                                memberIdentifierDto.isActive()
                ).findFirst();
        log.info("Does the account have SSN:{}", optionalAccountSSN.isEmpty());
        if(optionalAccountSSN.isEmpty()){
            // This means that there is no active SSN in the account for the member
            // Hence create the SSN that was received in the transaction
            createMemberIdentifier(member, transactionMemberDto);
            return;
        }
        // if the control reaches here, means that there is a SSN for the member in the transaction
        // and there is an active SSN for the member in the account
        String memberAccountSSN = optionalAccountSSN.get().getIdentifierValue();
        log.info("Account SSN:{}", memberAccountSSN);
        // Compare the two values to check if they are the same
        log.info("Is the transaction and account SSN same:{}",memberAccountSSN.equals(memberTransactionSSN));
        if(!memberAccountSSN.equals(memberTransactionSSN)){
            // If the SSNs are different then we needed to deactivate the previous SSN and activate the current SSN
            // Hence create the SSN that was received in the transaction
            createMemberIdentifier(member, transactionMemberDto);
            MemberIdentifierDto memberIdentifierDto = optionalAccountSSN.get();
            MemberIdentifier memberIdentifier = identifierMapper.identifierDtoToIdentifier(memberIdentifierDto);
            memberIdentifier.setMemberAcctIdentifierSK(memberIdentifierDto.getMemberIdentifierSK());
            // Set the active flag to false
            memberIdentifier.setActive(false);
            memberIdentifier.setChanged(true);
            memberIdentifier = memberIdentifierRepository.save(memberIdentifier);
            member.getMemberIdentifiers().add(memberIdentifier);
        }
        // If they are same then no action is needed
    }
}
