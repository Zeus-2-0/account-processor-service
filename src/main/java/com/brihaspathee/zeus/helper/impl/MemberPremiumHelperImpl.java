package com.brihaspathee.zeus.helper.impl;

import com.brihaspathee.zeus.domain.entity.Account;
import com.brihaspathee.zeus.domain.entity.Member;
import com.brihaspathee.zeus.domain.entity.MemberPremium;
import com.brihaspathee.zeus.domain.entity.PremiumSpan;
import com.brihaspathee.zeus.domain.repository.MemberPremiumRepository;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.account.MemberDto;
import com.brihaspathee.zeus.dto.account.MemberPremiumDto;
import com.brihaspathee.zeus.dto.account.PremiumSpanDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberIdentifierDto;
import com.brihaspathee.zeus.helper.interfaces.MemberHelper;
import com.brihaspathee.zeus.helper.interfaces.MemberPremiumHelper;
import com.brihaspathee.zeus.mapper.interfaces.MemberPremiumMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 01, December 2022
 * Time: 1:31 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.helper.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberPremiumHelperImpl implements MemberPremiumHelper {

    /**
     * Member Premium mapper instance
     */
    private final MemberPremiumMapper memberPremiumMapper;

    /**
     * Member Helper instance
     */
    private final MemberHelper memberHelper;

    /**
     * Member premium repository instance to perform CRUD operations
     */
    private final MemberPremiumRepository memberPremiumRepository;

    /**
     * Create member premiums - This method is called when we are adding the member's to premiums
     * in a ADD transactions
     * @param transactionMemberDtos
     * @param premiumSpan
     * @param members
     * @param coverageTypeCode
     */
    @Override
    public void createMemberPremiums(List<TransactionMemberDto> transactionMemberDtos,
                                     PremiumSpan premiumSpan,
                                     List<Member> members,
                                    String coverageTypeCode) {
        List<MemberPremium> memberPremiums = new ArrayList<>();
        members.stream().forEach(member -> {
            String relationshipType = member.getRelationShipTypeCode();
            /**
             * Coverage Type - Relationship Type - Action
             * DEP           - HOH               - Don't Add
             * DEP           - Not HOH           - Add
             * Not DEP       - HOH               - Add
             * Not DEP       - Not HOH           - Add
             */
            if(!(coverageTypeCode.equals("DEP") && relationshipType.equals("HOH"))){
                TransactionMemberDto transactionMemberDto = transactionMemberDtos.stream().filter(memberDto -> {
                    return memberDto.getTransactionMemberCode().equals(member.getTransactionMemberCode());
                }).findFirst().get();
                MemberPremium memberPremium = MemberPremium.builder()
                        .acctMemberSK(null)
                        .acctMemPremSK(null)
                        .premiumSpan(premiumSpan)
                        .member(member)
                        .exchangeMemberId(getExchangeMemberId(transactionMemberDto))
                        .individualRateAmount(transactionMemberDto.getMemberRate())
                        .build();
                memberPremium = memberPremiumRepository.save(memberPremium);
                memberPremiums.add(memberPremium);
            }
        });
        premiumSpan.setMemberPremiums(memberPremiums);

    }

    /**
     * Set member premiums to send to MMS
     * @param premiumSpanDto
     * @param premiumSpan
     */
    @Override
    public void setMemberPremiums(PremiumSpanDto premiumSpanDto, PremiumSpan premiumSpan) {
        if(premiumSpan.getMemberPremiums() != null && premiumSpan.getMemberPremiums().size() > 0){
            premiumSpanDto.setMemberPremiumSpans(
                    memberPremiumMapper
                            .memberPremiumsToMemberPremiumDtos(
                                    premiumSpan.getMemberPremiums())
                            .stream()
                            .collect(Collectors.toSet())
            );
        }
    }

    /**
     * Create member premiums for change transaction
     * @param matchedPremiumSpanDto
     * @param account
     * @param accountDto
     * @param transactionMemberDtos
     * @param premiumSpan
     */
    @Override
    public void createMemberPremiums(PremiumSpanDto matchedPremiumSpanDto,
                                     Account account,
                                     AccountDto accountDto,
                                     List<TransactionMemberDto> transactionMemberDtos,
                                     PremiumSpan premiumSpan){
        List<MemberPremium> memberPremiums = new ArrayList<>();
        Set<MemberPremiumDto> memberPremiumDtos = matchedPremiumSpanDto.getMemberPremiumSpans();
        // Perform the below logic on all members who are in the account prior to receipt of the transaction
        memberPremiumDtos.forEach(memberPremiumDto -> {
            String exchangeMemberId = memberPremiumDto.getExchangeMemberId();
            // Check if the member is present in the transaction
            Optional<TransactionMemberDto> optionalTransactionMemberDto = transactionMemberDtos.stream().filter(transactionMemberDto -> {
                String transactionExchangeMemberId = getExchangeMemberId(transactionMemberDto);
                return transactionExchangeMemberId.equals(exchangeMemberId);
            }).findFirst();
            if (optionalTransactionMemberDto.isEmpty()){
                // if the member is not present in the transaction
                // create an entry into the member table using the data from the account
                MemberDto memberDto = accountDto.getMembers()
                        .stream().filter(
                                memberDto1 ->
                                        memberDto1.getMemberCode()
                                                .equals(memberPremiumDto.getMemberCode()))
                        .findFirst()
                        .orElseThrow();
                Member member = memberHelper.createMember(memberDto, account);
                MemberPremium memberPremium = memberPremiumMapper.memberPremiumDtoToMemberPremium(memberPremiumDto);
                memberPremium.setPremiumSpan(premiumSpan);
                memberPremium.setMember(member);
                memberPremium = memberPremiumRepository.save(memberPremium);
                memberPremiums.add(memberPremium);
            }else{
                // if the member is already present in the transaction, they should already be present in the account
                TransactionMemberDto transactionMemberDto = optionalTransactionMemberDto.get();
                String transactionMemberCode = transactionMemberDto.getTransactionMemberCode();
                Member member = account.getMembers().stream()
                        .filter(member1 ->
                                member1.getTransactionMemberCode().equals(transactionMemberCode))
                        .findFirst()
                        .orElseThrow();
                // Check if the member is being termed or canceled in the transaction
//                if (! (transactionMemberDto.getTransactionTypeCode().equals("TERM") ||
//                        transactionMemberDto.getTransactionTypeCode().equals("CANCEL"))){
                if (! isMemberTermedOrCanceled(transactionMemberDto, premiumSpan.getStartDate().minusDays(1))){
                    // The member should be added to the premium span only if they are not termed or canceled
                    MemberPremium memberPremium = memberPremiumMapper.memberPremiumDtoToMemberPremium(memberPremiumDto);
                    memberPremium.setPremiumSpan(premiumSpan);
                    memberPremium.setMember(member);
                    memberPremium = memberPremiumRepository.save(memberPremium);
                    memberPremiums.add(memberPremium);
                }
            }
        });
        // Perform the below logic on all members who are added newly to the account in the transaction
        transactionMemberDtos.forEach(transactionMemberDto -> {
            // Get all the members who are received with transaction type code ADD
//            if (transactionMemberDto.getTransactionTypeCode().equals("ADD")){
            if (isMemberAdded(transactionMemberDto, premiumSpan.getStartDate())){
                String transactionMemberCode = transactionMemberDto.getTransactionMemberCode();
                Member member = account.getMembers()
                        .stream()
                        .filter(member1 -> member1.getTransactionMemberCode().equals(transactionMemberCode))
                        .findFirst().orElseThrow();
                MemberPremium memberPremium = MemberPremium.builder()
                        .premiumSpan(premiumSpan)
                        .member(member)
                        .exchangeMemberId(getExchangeMemberId(transactionMemberDto))
                        .individualRateAmount(transactionMemberDto.getMemberRate())
                        .build();
                memberPremium = memberPremiumRepository.save(memberPremium);
                memberPremiums.add(memberPremium);
            }
        });
        premiumSpan.setMemberPremiums(memberPremiums);
    }

    /**
     * Get the exchange member id of the member from the transaction
     * @param transactionMemberDto
     * @return
     */
    private String getExchangeMemberId(TransactionMemberDto transactionMemberDto){
        Optional<TransactionMemberIdentifierDto> optionalMemberIdentifier= transactionMemberDto.getIdentifiers().stream().filter(memberIdentifierDto -> {
            return memberIdentifierDto.getIdentifierTypeCode().equals("EXCHMEMID");
        }).findFirst();
        if(optionalMemberIdentifier.isPresent()){
            return optionalMemberIdentifier.get().getIdentifierValue();
        }else {
            return null;
        }
    }

    /**
     * Determine if the member in the transaction is being termed or canceled
     * @param transactionMemberDto
     * @param effectiveDate
     * @return
     */
    private boolean isMemberTermedOrCanceled(TransactionMemberDto transactionMemberDto, LocalDate effectiveDate){
        // Return true if the member is being canceled
        if (transactionMemberDto.getTransactionTypeCode().equals("CANCEL")){
            return true;
        }
        // If the member is being termed
        // Return True if they are being termed same date as the effective date
        // else return false
        if (transactionMemberDto.getTransactionTypeCode().equals("TERM")){
            return transactionMemberDto.getEffectiveDate().equals(effectiveDate) || transactionMemberDto.getEffectiveDate().isBefore(effectiveDate);
        } else {
            // If they are not being termed or canceled then return false
            return false;
        }
    }

    /**
     * Determine if the member is being added for the same effective date as the premium span
     * @param transactionMemberDto
     * @param effectiveDate
     * @return
     */
    private boolean isMemberAdded(TransactionMemberDto transactionMemberDto, LocalDate effectiveDate){
        // If the member is being Added
        // Return True if they are being added for the same date as the effective date
        // else return false
        if (transactionMemberDto.getTransactionTypeCode().equals("ADD")){
            return transactionMemberDto.getEffectiveDate().equals(effectiveDate) || transactionMemberDto.getEffectiveDate().isBefore(effectiveDate);
        } else {
            // If they are not being added then return false
            return false;
        }
    }
}
