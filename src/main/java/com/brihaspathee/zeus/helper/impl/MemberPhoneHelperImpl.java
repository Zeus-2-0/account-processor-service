package com.brihaspathee.zeus.helper.impl;

import com.brihaspathee.zeus.domain.entity.Member;
import com.brihaspathee.zeus.domain.entity.MemberPhone;
import com.brihaspathee.zeus.domain.repository.MemberPhoneRepository;
import com.brihaspathee.zeus.dto.account.MemberDto;
import com.brihaspathee.zeus.dto.account.MemberPhoneDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberPhoneDto;
import com.brihaspathee.zeus.helper.interfaces.MemberPhoneHelper;
import com.brihaspathee.zeus.mapper.interfaces.MemberPhoneMapper;
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
     * The utility class for account processor service
     */
    private final AccountProcessorUtil accountProcessorUtil;

    /**
     * Create member phone
     * @param member
     * @param transactionMemberDto
     * @param ztcn
     * @param source
     */
    @Override
    public void createMemberPhone(Member member,
                                  TransactionMemberDto transactionMemberDto,
                                  String ztcn,
                                  String source) {
        if(transactionMemberDto.getMemberPhones() != null && transactionMemberDto.getMemberPhones().size() > 0){
            List<MemberPhone> phones = new ArrayList<>();
            transactionMemberDto.getMemberPhones().forEach(phoneDto -> {
                String memberPhoneCode = accountProcessorUtil.generateUniqueCode(transactionMemberDto.getEntityCodes(),
                        "memberPhoneCode");
                MemberPhone memberPhone = MemberPhone.builder()
                        .member(member)
                        .memberAcctPhoneSK(null)
                        .memberPhoneCode(memberPhoneCode)
                        .phoneTypeCode(phoneDto.getPhoneTypeCode())
                        .phoneNumber(phoneDto.getPhoneNumber())
                        .ztcn(ztcn)
                        .source(source)
                        .startDate(phoneDto.getReceivedDate().toLocalDate())
                        .endDate(null)
                        .changed(true)
                        .build();
                memberPhone = memberPhoneRepository.save(memberPhone);
                phones.add(memberPhone);
            });
            member.setMemberPhones(phones);
        }
    }

    /**
     * Create the member phone in the repository
     * @param member
     * @param transactionMemberPhoneDto
     * @param memberPhoneCode
     * @param ztcn
     * @param source
     * @return
     */
    private MemberPhone createMemberPhone(Member member,
                                          TransactionMemberPhoneDto transactionMemberPhoneDto,
                                          String memberPhoneCode, String ztcn,
                                          String source){
        MemberPhone memberPhone = MemberPhone.builder()
                .member(member)
                .memberAcctPhoneSK(null)
                .memberPhoneCode(memberPhoneCode)
                .phoneTypeCode(transactionMemberPhoneDto.getPhoneTypeCode())
                .phoneNumber(transactionMemberPhoneDto.getPhoneNumber())
                .ztcn(ztcn)
                .source(source)
                .startDate(transactionMemberPhoneDto.getReceivedDate().toLocalDate())
                .endDate(null)
                .changed(true)
                .build();
        memberPhone = memberPhoneRepository.save(memberPhone);
        return memberPhone;
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

    /**
     * Match member phone from the transaction to the account
     * @param member
     * @param memberDto
     * @param transactionMemberDto
     * @param ztcn
     * @param source
     */
    @Override
    public void matchMemberPhone(Member member,
                                 MemberDto memberDto,
                                 TransactionMemberDto transactionMemberDto,
                                 String ztcn,
                                 String source) {
//        log.info("Inside member match phone");
        // check if the transaction has any languages for the member
        // if there are no languages in the transaction then return
        if (transactionMemberDto.getMemberPhones() == null ||
            transactionMemberDto.getMemberPhones().isEmpty()){
            return;
        }
        // check for each phone types possible
        matchMemberPhone("ALT", member, memberDto, transactionMemberDto, ztcn, source);
        matchMemberPhone("BEEPER", member, memberDto, transactionMemberDto, ztcn, source);
        matchMemberPhone("CELL", member, memberDto, transactionMemberDto, ztcn, source);
        matchMemberPhone("EXT", member, memberDto, transactionMemberDto, ztcn, source);
        matchMemberPhone("FAX", member, memberDto, transactionMemberDto, ztcn, source);
        matchMemberPhone("HOME", member, memberDto, transactionMemberDto, ztcn, source);
        matchMemberPhone("WORK", member, memberDto, transactionMemberDto, ztcn, source);

    }

    /**
     * Match the specific phone type
     * @param phoneTypeCode
     * @param member
     * @param memberDto
     * @param transactionMemberDto
     * @param ztcn
     * @param source
     */
    private void matchMemberPhone(String phoneTypeCode,
                                  Member member,
                                  MemberDto memberDto,
                                  TransactionMemberDto transactionMemberDto,
                                  String ztcn,
                                  String source){
        List<MemberPhone> phones = new ArrayList<>();
        // Check if the transactions has the passed in phone type for the member
        Optional<TransactionMemberPhoneDto> optionalTransactionPhoneDto = transactionMemberDto.getMemberPhones()
                .stream()
                .filter(
                        transactionMemberPhoneDto ->
                                transactionMemberPhoneDto.getPhoneTypeCode().equals(phoneTypeCode)
                ).findFirst();
        // if there are no phones of the passed in type, then return - nothing to compare here
        if(optionalTransactionPhoneDto.isEmpty()){
            return;
        }
        // if the control reaches here, then the transaction contains the phone of the passed type
        TransactionMemberPhoneDto transactionPhoneDto = optionalTransactionPhoneDto.get();
        // See if the account has the same phone type
        Optional<MemberPhoneDto> optionalAccountPhoneDto = memberDto.getMemberPhones()
                .stream()
                .filter(
                        phoneDto ->
                                phoneDto.getPhoneTypeCode().equals(phoneTypeCode) &&
                                        phoneDto.getEndDate() == null
                ).findFirst();
        if (optionalAccountPhoneDto.isEmpty()){
            // this means that the account does not have that phone type
            // create the phone received in the transaction
            // since this will be a new phone create the phone code
            String memberPhoneCode = accountProcessorUtil.generateUniqueCode(transactionMemberDto.getEntityCodes(),
                    "memberPhoneCode");
            MemberPhone memberPhone = createMemberPhone(member,
                    transactionPhoneDto,
                    memberPhoneCode, ztcn, source);
            phones.add(memberPhone);
            return;
        }
        // if the control reaches here, then the transaction and the account has the same language
        MemberPhoneDto accountPhoneDto = optionalAccountPhoneDto.get();
        // compare the phone numbers
        if (!transactionPhoneDto.getPhoneNumber()
                .equals(accountPhoneDto.getPhoneNumber())){
            // the phone numbers are different
            // create the phone number as it is received in the transaction
            String memberPhoneCode = accountProcessorUtil.generateUniqueCode(transactionMemberDto.getEntityCodes(),
                    "memberPhoneCode");
            MemberPhone memberPhone = createMemberPhone(member,
                    transactionPhoneDto,
                    memberPhoneCode, ztcn, source);
            phones.add(memberPhone);
            // set the end date of the phone number in the account to one day prior to the
            // transaction received date
            accountPhoneDto.setEndDate(transactionPhoneDto.getReceivedDate().minusDays(1).toLocalDate());
            MemberPhone updatedPhone = phoneMapper.phoneDtoToPhone(accountPhoneDto);
            // set the phone sk of the phone in MMS
            updatedPhone.setMemberAcctPhoneSK(accountPhoneDto.getMemberPhoneSK());
            // set the changed flag to true
            updatedPhone.setChanged(true);
            // save the phone to the repository
            updatedPhone = memberPhoneRepository.save(updatedPhone);
            // add the phone to the list
            phones.add(updatedPhone);
        }
        if(member.getMemberPhones() == null ||
            member.getMemberPhones().isEmpty()){
            member.setMemberPhones(phones);
        }else{
            member.getMemberPhones().addAll(phones);
        }
    }
}
