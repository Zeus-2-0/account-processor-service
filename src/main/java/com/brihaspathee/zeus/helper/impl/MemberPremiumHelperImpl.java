package com.brihaspathee.zeus.helper.impl;

import com.brihaspathee.zeus.domain.entity.Member;
import com.brihaspathee.zeus.domain.entity.MemberPremium;
import com.brihaspathee.zeus.domain.entity.PremiumSpan;
import com.brihaspathee.zeus.domain.repository.MemberPremiumRepository;
import com.brihaspathee.zeus.dto.account.PremiumSpanDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberIdentifierDto;
import com.brihaspathee.zeus.helper.interfaces.MemberPremiumHelper;
import com.brihaspathee.zeus.mapper.interfaces.MemberPremiumMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
     * Member premium repository instance to perform CRUD operations
     */
    private final MemberPremiumRepository memberPremiumRepository;

    /**
     * Create member premiums
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
}
