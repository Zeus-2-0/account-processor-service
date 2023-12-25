package com.brihaspathee.zeus.helper.impl;

import com.brihaspathee.zeus.domain.entity.Account;
import com.brihaspathee.zeus.domain.entity.Member;
import com.brihaspathee.zeus.domain.entity.MemberAddress;
import com.brihaspathee.zeus.domain.repository.MemberAddressRepository;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.account.MemberAddressDto;
import com.brihaspathee.zeus.dto.account.MemberDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberAddressDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberDto;
import com.brihaspathee.zeus.helper.interfaces.MemberAddressHelper;
import com.brihaspathee.zeus.mapper.interfaces.MemberAddressMapper;
import com.brihaspathee.zeus.util.AccountProcessorUtil;
import com.brihaspathee.zeus.util.ZeusRandomStringGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
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
     * The utility class for account processor service
     */
    private final AccountProcessorUtil accountProcessorUtil;

    /**
     * Create a member address
     * @param member
     * @param transactionMemberDto
     */
    @Override
    public void createMemberAddress(Member member, TransactionMemberDto transactionMemberDto) {
        if(transactionMemberDto.getMemberAddresses() != null && transactionMemberDto.getMemberAddresses().size() > 0){
            List<MemberAddress> addresses = new ArrayList<>();
            transactionMemberDto.getMemberAddresses().forEach(addressDto -> {
                // create the address received in the transaction
                // Since this will be a new address create a new member code
                String memberAddressCode = accountProcessorUtil.generateUniqueCode(transactionMemberDto.getEntityCodes(),
                        "memberAddressCode");
                MemberAddress memberAddress = createMemberAddress(member, addressDto, memberAddressCode);
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

    /**
     * Compare ana match the member addresses from the transaction and account
     * @param member
     * @param memberDto
     * @param transactionMemberDto
     */
    @Override
    public void matchMemberAddress(Member member, MemberDto memberDto, TransactionMemberDto transactionMemberDto) {
//        log.info("Inside member match address");
        // Check if the transaction has any addresses for the member
        // If there are no addresses in the transaction then return
        if (transactionMemberDto.getMemberAddresses() == null ||
                transactionMemberDto.getMemberAddresses().size() == 0){
            return;
        }
        // There are only two types of addresses possible "RESIDENCE" and "MAIL"
        matchAddress("RESIDENCE", member, memberDto, transactionMemberDto);
        matchAddress("MAIL", member, memberDto, transactionMemberDto);
    }

    /**
     * Match each address types in the transaction and compare it with the address in the account
     * @param addressTypeCode
     * @param member
     * @param memberDto
     * @param transactionMemberDto
     */
    private void matchAddress(String addressTypeCode, Member member, MemberDto memberDto, TransactionMemberDto transactionMemberDto) {
        List<MemberAddress> addresses = new ArrayList<>();

        // Check if the transaction has the passed in address type for the member
        Optional<TransactionMemberAddressDto> optionalTransactionMemberResAddressDto = transactionMemberDto.getMemberAddresses().stream()
                .filter(
                        transactionMemberAddressDto ->
                                transactionMemberAddressDto.getAddressTypeCode().equals(addressTypeCode))
                .findFirst();
        if (optionalTransactionMemberResAddressDto.isPresent()){
            // get the residential address from the account that has null as the end date
            Optional<MemberAddressDto> optionalMemberAddressDto = memberDto.getMemberAddresses().stream()
                    .filter(
                            memberAddressDto ->
                                    memberAddressDto.getAddressTypeCode().equals(addressTypeCode) &&
                                    memberAddressDto.getEndDate() == null
                    ).findFirst();
            if(optionalMemberAddressDto.isPresent()){
                // compare the address from the transaction and the address from the account to see if there
                // are any changes
                if(compareAddress(optionalTransactionMemberResAddressDto.get(),
                        optionalMemberAddressDto.get())){
                    // if the address in the transaction is different from the address in the account then create the new address
                    String memberAddressCode = accountProcessorUtil.generateUniqueCode(transactionMemberDto.getEntityCodes(),
                            "memberAddressCode");
                    MemberAddress memberAddress = createMemberAddress(member, optionalTransactionMemberResAddressDto.get(), memberAddressCode);
                    addresses.add(memberAddress);
                    // Update the address in the account to term one day prior to the transaction received date
                    // Get the current residential address in the account
                    MemberAddressDto currentResAddress = optionalMemberAddressDto.get();
                    // set the end date to be one date prior to the transaction received date
                    currentResAddress.setEndDate(optionalTransactionMemberResAddressDto.get().getReceivedDate().minusDays(1).toLocalDate());
                    MemberAddress updatedAddress = memberAddressMapper.memberAddressDtoToMemberAddress(currentResAddress);
                    // Set the address sk of the address in MMS
                    updatedAddress.setMemberAcctAddressSK(currentResAddress.getMemberAddressSK());
                    // Set the changed flag to true
                    updatedAddress.setChanged(true);
                    // save the address to the repository
                    updatedAddress = memberAddressRepository.save(updatedAddress);
                    // add the addresses to the list
                    addresses.add(updatedAddress);
                }
            }else{
                // create the address received in the transaction
                // Since this will be a new address create a new address code
                String memberAddressCode = accountProcessorUtil.generateUniqueCode(transactionMemberDto.getEntityCodes(),
                        "memberAddressCode");
                MemberAddress memberAddress = createMemberAddress(member, optionalTransactionMemberResAddressDto.get(), memberAddressCode);
                addresses.add(memberAddress);
            }
        }
        if(member.getMemberAddresses() == null || member.getMemberAddresses().isEmpty()){
            member.setMemberAddresses(addresses);
        }else {
            member.getMemberAddresses().addAll(addresses);
        }

    }

    /**
     * Create the address in the repository using the address code provided
     * @param member
     * @param transactionMemberAddressDto
     * @param memberAddressCode
     * @return
     */
    private MemberAddress createMemberAddress(Member member, TransactionMemberAddressDto transactionMemberAddressDto, String memberAddressCode){
        MemberAddress memberAddress = MemberAddress.builder()
                .member(member)
                .memberAcctAddressSK(null)
                .memberAddressCode(memberAddressCode)
                .addressTypeCode(transactionMemberAddressDto.getAddressTypeCode())
                .addressLine1(transactionMemberAddressDto.getAddressLine1())
                .addressLine2(transactionMemberAddressDto.getAddressLine2())
                .city(transactionMemberAddressDto.getCity())
                .stateTypeCode(transactionMemberAddressDto.getStateTypeCode())
                .zipCode(transactionMemberAddressDto.getZipCode())
                .countyCode(transactionMemberAddressDto.getCountyCode())
                .startDate(transactionMemberAddressDto.getReceivedDate().toLocalDate())
                .endDate(null)
                .changed(true)
                .build();
        memberAddress = memberAddressRepository.save(memberAddress);
        return memberAddress;
    }

    /**
     * Compare the address from the transaction and the address from the member to see if they are the same
     * @param transactionMemberAddressDto
     * @param memberAddressDto
     * @return
     */
    private boolean compareAddress(TransactionMemberAddressDto transactionMemberAddressDto, MemberAddressDto memberAddressDto){
        if(!transactionMemberAddressDto.getAddressLine1().equals(memberAddressDto.getAddressLine1())){
            return true;
        }
        if(transactionMemberAddressDto.getAddressLine2() != null && memberAddressDto.getAddressLine2() !=null &&
                !transactionMemberAddressDto.getAddressLine2().equals(memberAddressDto.getAddressLine2())){
            return true;
        }
        if(!transactionMemberAddressDto.getCity().equals(memberAddressDto.getCity())){
            return true;
        }
        if(!transactionMemberAddressDto.getStateTypeCode().equals(memberAddressDto.getStateTypeCode())){
            return true;
        }
        if(!transactionMemberAddressDto.getZipCode().equals(memberAddressDto.getZipCode())){
            return true;
        }
        return false;
    }

    /**
     * Convert the transaction member address dto object to member address dto object
     * @param transactionMemberAddressDto
     * @return
     */
    private MemberAddressDto mapTransactionAddressToMemberAddressDto(TransactionMemberAddressDto transactionMemberAddressDto){
        return MemberAddressDto.builder()
                .addressTypeCode(transactionMemberAddressDto.getAddressTypeCode())
                .addressLine1(transactionMemberAddressDto.getAddressLine1())
                .addressLine1(transactionMemberAddressDto.getAddressLine2())
                .city(transactionMemberAddressDto.getCity())
                .stateTypeCode(transactionMemberAddressDto.getStateTypeCode())
                .zipCode(transactionMemberAddressDto.getZipCode())
                .build();
    }
}
