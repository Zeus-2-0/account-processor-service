package com.brihaspathee.zeus.helper.impl;

import com.brihaspathee.zeus.domain.entity.Account;
import com.brihaspathee.zeus.domain.entity.Member;
import com.brihaspathee.zeus.domain.entity.MemberAddress;
import com.brihaspathee.zeus.domain.repository.MemberAddressRepository;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.account.MemberDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberDto;
import com.brihaspathee.zeus.helper.interfaces.MemberAddressHelper;
import com.brihaspathee.zeus.mapper.interfaces.MemberAddressMapper;
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
 * Time: 6:16 AM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.helper.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberAddressHelperImpl implements MemberAddressHelper {

    /**
     * Member address mapper instance
     */
    private final MemberAddressMapper memberAddressMapper;

    /**
     * Member Address Repository instance to perform CRUD operations
     */
    private final MemberAddressRepository memberAddressRepository;

    /**
     * Create a member address
     * @param member
     * @param transactionMemberDto
     */
    @Override
    public void createMemberAddress(Member member, TransactionMemberDto transactionMemberDto) {
        if(transactionMemberDto.getMemberAddresses() != null && transactionMemberDto.getMemberAddresses().size() > 0){
            List<MemberAddress> addresses = new ArrayList<>();
            transactionMemberDto.getMemberAddresses().stream().forEach(addressDto -> {
                MemberAddress memberAddress = MemberAddress.builder()
                        .member(member)
                        .memberAcctAddressSK(null)
                        .memberAddressCode(ZeusRandomStringGenerator.randomString(15))
                        .addressTypeCode(addressDto.getAddressTypeCode())
                        .addressLine1(addressDto.getAddressLine1())
                        .addressLine2(addressDto.getAddressLine2())
                        .city(addressDto.getCity())
                        .stateTypeCode(addressDto.getStateTypeCode())
                        .zipCode(addressDto.getZipCode())
                        .countyCode(addressDto.getCountyCode())
                        .startDate(addressDto.getReceivedDate().toLocalDate())
                        .endDate(null)
                        .build();
                memberAddress = memberAddressRepository.save(memberAddress);
                addresses.add(memberAddress);
            });
            member.setMemberAddresses(addresses);
        }
    }

    /**
     * Set member address
     * @param memberDto
     * @param member
     */
    @Override
    public void setMemberAddress(MemberDto memberDto, Member member) {
        if(member.getMemberAddresses() != null && member.getMemberAddresses().size() >0){
            memberDto.setMemberAddresses(
                    memberAddressMapper
                            .memberAddressesToMemberAddressDtos(
                                    member.getMemberAddresses())
                            .stream()
                            .collect(Collectors.toSet())
            );
        }
    }
}
