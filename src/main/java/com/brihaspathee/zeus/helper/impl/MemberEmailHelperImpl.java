package com.brihaspathee.zeus.helper.impl;

import com.brihaspathee.zeus.domain.entity.Member;
import com.brihaspathee.zeus.domain.entity.MemberEmail;
import com.brihaspathee.zeus.domain.repository.MemberEmailRepository;
import com.brihaspathee.zeus.dto.account.MemberDto;
import com.brihaspathee.zeus.dto.account.MemberEmailDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberEmailDto;
import com.brihaspathee.zeus.helper.interfaces.MemberEmailHelper;
import com.brihaspathee.zeus.mapper.interfaces.MemberEmailMapper;
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
     * Member email helper instance
     */
    private final MemberEmailMapper emailMapper;

    /**
     * Member email repository instance to perform CRUD operations
     */
    private final MemberEmailRepository memberEmailRepository;

    /**
     * The utility class for account processor service
     */
    private final AccountProcessorUtil accountProcessorUtil;

    /**
     * Create the member email
     * @param member
     * @param transactionMemberDto
     * @param ztcn
     * @param source
     */
    @Override
    public void createMemberEmail(Member member,
                                  TransactionMemberDto transactionMemberDto,
                                  String ztcn,
                                  String source) {
        if(transactionMemberDto.getEmails() != null && transactionMemberDto.getEmails().size() > 0){
            List<MemberEmail> emails = new ArrayList<>();
            transactionMemberDto.getEmails().forEach(emailDto -> {
                String memberEmailCode = accountProcessorUtil.generateUniqueCode(transactionMemberDto.getEntityCodes(),
                        "memberEmailCode");
                MemberEmail memberEmail = MemberEmail.builder()
                        .member(member)
                        .memberAcctEmailSK(null)
                        .memberEmailCode(memberEmailCode)
                        .emailTypeCode("PERSONAL")
                        .email(emailDto.getEmail())
                        .isPrimary(true)
                        .ztcn(ztcn)
                        .source(source)
                        .startDate(emailDto.getReceivedDate().toLocalDate())
                        .endDate(null)
                        .changed(true)
                        .build();
                memberEmail = memberEmailRepository.save(memberEmail);
                emails.add(memberEmail);
            });
            member.setMemberEmails(emails);
        }
    }

    /**
     * Create the member email
     * @param member
     * @param transactionMemberEmailDto
     * @param memberEmailCode
     * @param ztcn
     * @param source
     * @return
     */
    private MemberEmail createMemberEmail(Member member,
                                          TransactionMemberEmailDto transactionMemberEmailDto,
                                          String memberEmailCode, String ztcn,
                                          String source){
        MemberEmail memberEmail = MemberEmail.builder()
                .member(member)
                .memberAcctEmailSK(null)
                .memberEmailCode(memberEmailCode)
                .email(transactionMemberEmailDto.getEmail())
                .emailTypeCode("PERSONAL")
                .isPrimary(true)
                .ztcn(ztcn)
                .source(source)
                .startDate(transactionMemberEmailDto.getReceivedDate().toLocalDate())
                .endDate(null)
                .changed(true)
                .build();
        memberEmail = memberEmailRepository.save(memberEmail);
        return memberEmail;
    }

    /**
     * Set the member email dto to  send to MMS
     * @param memberDto
     * @param member
     */
    @Override
    public void setMemberEmail(MemberDto memberDto, Member member) {
        if(member.getMemberEmails() != null && member.getMemberEmails().size() >0){
            memberDto.setMemberEmails(
                    emailMapper
                            .emailsToEmailDtos(
                                    member.getMemberEmails())
                            .stream()
                            .collect(Collectors.toSet())
            );
        }
    }

    /**
     * Match the member's email from the account to the email in the transaction
     * @param member
     * @param memberDto
     * @param transactionMemberDto
     * @param ztcn
     * @param source
     */
    @Override
    public void matchMemberEmail(Member member,
                                 MemberDto memberDto,
                                 TransactionMemberDto transactionMemberDto,
                                 String ztcn,
                                 String source) {
//        log.info("Inside member match email");
        // Check if the transaction has any emails for the member
        // If there is no email in the transaction then return
        if(transactionMemberDto.getEmails() == null ||
                transactionMemberDto.getEmails().isEmpty()){
            return;
        }
        // Compare the below email types
        matchMemberEmail("PERSONAL", member, memberDto, transactionMemberDto, ztcn, source);
    }

    /**
     * Match member's specific email type
     * @param emailTypeCode
     * @param member
     * @param memberDto
     * @param transactionMemberDto
     * @param ztcn
     * @param source
     */
    private void matchMemberEmail(String emailTypeCode,
                                  Member member,
                                  MemberDto memberDto,
                                  TransactionMemberDto transactionMemberDto,
                                  String ztcn,
                                  String source){
        List<MemberEmail> emails = new ArrayList<>();
        // if there are no emails then return
        if(transactionMemberDto.getEmails() == null ||
                transactionMemberDto.getEmails().isEmpty()){
            return;
        }
        // There can be only one email in the transaction get the first email from the list
        TransactionMemberEmailDto transactionEmailDto = transactionMemberDto.getEmails().get(0);
        // the email from the transaction has to be compared with the member's personal email
        // See if the member has a personal email in the account
        Optional<MemberEmailDto> optionalMemberEmailDto = memberDto.getMemberEmails()
                .stream()
                .filter(
                        emailDto ->
                                emailDto.getEmailTypeCode().equals(emailTypeCode) &&
                                        emailDto.getEndDate() == null
                ).findFirst();
        if (optionalMemberEmailDto.isEmpty()){
            // this meas that the account does not have a personal email
            // create the personal email received in the transaction
            // Since this will be a new email create the email code
            String memberEmailCode = accountProcessorUtil.generateUniqueCode(transactionMemberDto.getEntityCodes(),
                    "memberEmailCode");
            MemberEmail memberEmail = createMemberEmail(member,
                    transactionEmailDto,
                    memberEmailCode, ztcn, source);
            emails.add(memberEmail);
            return;
        }
        // if the control reaches here, then the transaction and the account have personal email
        MemberEmailDto accountEmailDto = optionalMemberEmailDto.get();
        // compare the emails
        if (!transactionEmailDto.getEmail().equals(accountEmailDto.getEmail())){
            // the personal emails are different
            // create the language received in the transaction
            String memberEmailCode = accountProcessorUtil.generateUniqueCode(transactionMemberDto.getEntityCodes(),
                    "memberEmailCode");
            MemberEmail memberEmail = createMemberEmail(member,
                    transactionEmailDto,
                    memberEmailCode, ztcn, source);
            emails.add(memberEmail);
            // set the end date of the email in the account to one day prior to the
            // transaction received date
            accountEmailDto.setEndDate(transactionEmailDto.getReceivedDate().minusDays(1).toLocalDate());
            MemberEmail updatedEmail = emailMapper.emailDtoToEmail(accountEmailDto);
            // set the email sk of the email in MMS
            updatedEmail.setMemberAcctEmailSK(accountEmailDto.getMemberEmailSK());
            // set the changed flag to true
            updatedEmail.setChanged(true);
            // set the primary flag to false
            updatedEmail.setPrimary(false);
            // save the email to the repository
            updatedEmail = memberEmailRepository.save(updatedEmail);
            // add the language to the list
            emails.add(updatedEmail);
        }
        if (member.getMemberEmails() == null || member.getMemberEmails().isEmpty()){
            member.setMemberEmails(emails);
        }else {
            member.getMemberEmails().addAll(emails);
        }
    }
}
