package com.brihaspathee.zeus.helper.impl;

import com.brihaspathee.zeus.domain.entity.AlternateContact;
import com.brihaspathee.zeus.domain.entity.Member;
import com.brihaspathee.zeus.domain.repository.AlternateContactRepository;
import com.brihaspathee.zeus.dto.account.MemberDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberDto;
import com.brihaspathee.zeus.helper.interfaces.AlternateContactHelper;
import com.brihaspathee.zeus.mapper.interfaces.AlternateContactMapper;
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
 * Time: 8:48 AM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.helper.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlternateContactHelperImpl implements AlternateContactHelper {

    /**
     * Alternate contact mapper instance
     */
    private final AlternateContactMapper alternateContactMapper;

    /**
     * Alternate contact repository instance to perform CRUD operations
     */
    private final AlternateContactRepository alternateContactRepository;

    /**
     * The utility class for account processor service
     */
    private final AccountProcessorUtil accountProcessorUtil;

    /**
     * Create alternate contact
     * @param member
     * @param transactionMemberDto
     */
    @Override
    public void createAlternateContact(Member member, TransactionMemberDto transactionMemberDto) {

        List<AlternateContact> alternateContacts = new ArrayList<>();
        if(transactionMemberDto.getAlternateContacts() != null &&
        transactionMemberDto.getAlternateContacts().size() > 0){
            transactionMemberDto.getAlternateContacts().forEach(alternateContactDto -> {
                String alternateContactCode = accountProcessorUtil.generateUniqueCode(transactionMemberDto.getEntityCodes(),
                        "alternateContactCode");
                AlternateContact alternateContact = AlternateContact.builder()
                        .member(member)
                        .acctAltContactSK(null)
                        .alternateContactCode(alternateContactCode)
                        .alternateContactTypeCode(alternateContactDto.getAlternateContactTypeCode())
                        .firstName(alternateContactDto.getFirstName())
                        .lastName(alternateContactDto.getLastName())
                        .middleName(alternateContactDto.getMiddleName())
                        .identifierTypeCode(alternateContactDto.getIdentifierTypeCode())
                        .identifierValue(alternateContactDto.getIdentifierValue())
                        .phoneTypeCode(alternateContactDto.getPhoneTypeCode())
                        .phoneNumber(alternateContactDto.getPhoneNumber())
                        .email(alternateContactDto.getEmail())
                        .addressLine1(alternateContactDto.getAddressLine1())
                        .addressLine2(alternateContactDto.getAddressLine2())
                        .city(alternateContactDto.getCity())
                        .stateTypeCode(alternateContactDto.getStateTypeCode())
                        .zipCode(alternateContactDto.getZipCode())
                        .startDate(alternateContactDto.getReceivedDate().toLocalDate())
                        .endDate(null)
                        .changed(true)
                        .build();
                alternateContact = alternateContactRepository.save(alternateContact);
                alternateContacts.add(alternateContact);
            });
            member.setAlternateContacts(alternateContacts);
        }

    }

    /**
     * Set Alternate contact dto to send to MMS
     * @param memberDto
     * @param member
     */
    @Override
    public void setAlternateContact(MemberDto memberDto, Member member) {
        if(member.getAlternateContacts() != null && member.getAlternateContacts().size() >0){
            memberDto.setAlternateContacts(
                    alternateContactMapper
                            .alternateContactsToAlternateContactDtos(
                                    member.getAlternateContacts())
                            .stream()
                            .collect(Collectors.toSet())
            );
        }
    }
}
