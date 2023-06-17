package com.brihaspathee.zeus.helper.impl;

import com.brihaspathee.zeus.domain.entity.Account;
import com.brihaspathee.zeus.domain.entity.Member;
import com.brihaspathee.zeus.domain.repository.MemberRepository;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.account.MemberDto;
import com.brihaspathee.zeus.dto.account.MemberIdentifierDto;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberIdentifierDto;
import com.brihaspathee.zeus.helper.interfaces.*;
import com.brihaspathee.zeus.mapper.interfaces.MemberMapper;
import com.brihaspathee.zeus.util.AccountProcessorUtil;
import com.brihaspathee.zeus.util.ZeusRandomStringGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
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
     * The utility class for account processor service
     */
    private final AccountProcessorUtil accountProcessorUtil;

    /**
     * Create member
     * @param members member present in the account
     * @param account account to which the members need to be added
     */
    @Override
    public List<Member> createMembers(List<TransactionMemberDto> members,
                                      Account account) {
        List<Member> savedMembers = new ArrayList<>();
        members.forEach(transactionMemberDto -> {
            Member member = createMember(account, transactionMemberDto, null);
            memberAddressHelper.createMemberAddress(member, transactionMemberDto);
            memberEmailHelper.createMemberEmail(member, transactionMemberDto);
            memberPhoneHelper.createMemberPhone(member, transactionMemberDto);
            memberLanguageHelper.createMemberLanguage(member, transactionMemberDto);
            memberIdentifierHelper.createMemberIdentifier(member, transactionMemberDto);
            alternateContactHelper.createAlternateContact(member, transactionMemberDto);
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
        if(account.getMembers() != null && account.getMembers().size() > 0){
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

    /**
     * Match the members in the transaction with members in the account
     * @param accountDto The account dto that contains the members in the account
     * @param transactionDto The transaction dto that contains the members in the transaction
     * @param account The account entity to which the matched members have to be added
     */
    @Override
    public void matchMember(AccountDto accountDto, TransactionDto transactionDto, Account account) {
        // if account only has one member then that member should be "HOH" and that should be the member
        // in the transaction as well because that's how the account is matched in MMS
        // So when the account has only one member then set that member in the account entity and return
        if(accountDto.getMembers() == null || transactionDto.getMembers() == null){
            return;
        }
        if(accountDto.getMembers().size() == 1 && transactionDto.getMembers().size() == 1){
            MemberDto primarySubscriber = accountDto.getMembers().stream().findFirst().orElseThrow();
            TransactionMemberDto transactionMemberDto = transactionDto.getMembers().get(0);
            Member member = createMember(account,
                    transactionMemberDto,
                    primarySubscriber);
            account.setMembers(List.of(member));
        }else{
            // List to hold all the members from the transaction
            List<Member> members = new ArrayList<>();
            transactionDto.getMembers().forEach(transactionMemberDto -> {
                MemberDto memberDto = getMatchedMember(transactionMemberDto, accountDto.getMembers());
                Member member;
                if(memberDto == null){
                    // Member is not present in MMS - This means it's a new member
                    // Create the member in the repository
                    member = createMember(account,
                            transactionMemberDto,
                            null);
                }else{
                    // Member is present in MMS - This means it's not a new member
                    // Create the member in the repository
                    member = createMember(account,
                            transactionMemberDto,
                            memberDto);
                }
                // Add the member to the list
                members.add(member);
            });
            // Add these members to the account
            account.setMembers(members);
        }
    }

    /**
     * Get the member who matches to the member in the transaction
     * @param transactionMemberDto the member in the transaction
     * @param members The members in the account
     * @return return the matched member if present else return null
     */
    private MemberDto getMatchedMember(TransactionMemberDto transactionMemberDto, Set<MemberDto> members) {

        Optional<MemberDto> optionalMatchedMember = members.stream()
                .filter(
                        memberDto -> isMemberMatch(transactionMemberDto, memberDto))
                .findFirst();
        return optionalMatchedMember.orElse(null);
    }

    /**
     * Create an individual member
     * @param account account to which the member needs to be added
     * @param transactionMemberDto details of the member present in the transaction
     * @param memberDto member dto if the member was matched in MMS
     * @return returns the created member
     */
    private Member createMember(Account account,
                                TransactionMemberDto transactionMemberDto,
                                MemberDto memberDto) {
        log.info("member detail:{}", transactionMemberDto);
        Member member = Member.builder()
                .account(account)
                // The member code assigned for the member in the transaction manager service
                .transactionMemberCode(transactionMemberDto.getTransactionMemberCode())
                // If the member is present in the account their relationship should be the same
                .relationShipTypeCode(transactionMemberDto.getRelationshipTypeCode())
                .changed(true)
                .build();
        // the member was matched in MMS then set the member SK and member code from MMS
        if(memberDto != null){
            member.setAcctMemberSK(memberDto.getMemberSK());
            member.setMemberCode(memberDto.getMemberCode());
            // if there was no change to the member from the transaction then set the details from MMS
            boolean changed = doMemberChange(transactionMemberDto, memberDto);
            if(!changed){
                member.setFirstName(memberDto.getFirstName());
                member.setMiddleName(memberDto.getMiddleName());
                member.setLastName(memberDto.getLastName());
                member.setDateOfBirth(memberDto.getDateOfBirth());
                member.setGenderTypeCode(memberDto.getGenderTypeCode());
                member.setTobaccoInd(false);
                member.setHeight(memberDto.getHeight());
                member.setWeight(memberDto.getWeight());
                member.setChanged(changed);
            } // if there was a change in the transaction then set the details from the transaction if they are present
            else{
                member.setFirstName(transactionMemberDto.getFirstName());
                // If the transaction has a middle name then set it from the transaction
                // if it is not present in the transaction then set it from the member matched in MMS
                if(transactionMemberDto.getMiddleName() != null){
                    member.setMiddleName(transactionMemberDto.getMiddleName());
                }else{
                    member.setMiddleName(memberDto.getMiddleName());
                }
                member.setLastName(transactionMemberDto.getLastName());
                // If the transaction has a date of birth then set it from the transaction
                // if it is not present in the transaction then set it from the member matched in MMS
                if(transactionMemberDto.getDateOfBirth() != null){
                    member.setDateOfBirth(transactionMemberDto.getDateOfBirth());
                }else{
                    member.setDateOfBirth(memberDto.getDateOfBirth());
                }
                // If the transaction has a gender then set it from the transaction
                // if it is not present in the transaction then set it from the member matched in MMS
                if(transactionMemberDto.getGenderTypeCode() != null){
                    member.setGenderTypeCode(transactionMemberDto.getGenderTypeCode());
                }else{
                    member.setGenderTypeCode(memberDto.getGenderTypeCode());
                }
                member.setTobaccoInd(false);
                member.setChanged(changed);

            }
        } // Else set the member sk to null and create a new member code
        else{
            member.setAcctMemberSK(null);
            member.setMemberCode(accountProcessorUtil.generateUniqueCode(transactionMemberDto.getEntityCodes(),
                    "memberCode"));
            member.setRelationShipTypeCode(transactionMemberDto.getRelationshipTypeCode());
            member.setFirstName(transactionMemberDto.getFirstName());
            member.setMiddleName(transactionMemberDto.getMiddleName());
            member.setLastName(transactionMemberDto.getLastName());
            member.setDateOfBirth(transactionMemberDto.getDateOfBirth());
            member.setGenderTypeCode(transactionMemberDto.getGenderTypeCode());
            member.setTobaccoInd(false);
            member.setChanged(true);
        }
        member = memberRepository.save(member);

        return member;
    }

    /**
     * Check if the member from the transaction and the member from the account are a match
     * @param transactionMemberDto
     * @param accountMemberDto
     * @return
     */
    private boolean isMemberMatch(TransactionMemberDto transactionMemberDto, MemberDto accountMemberDto){
        // Get the SSN of the member if present in the transaction
        Optional<TransactionMemberIdentifierDto> optionalMemberTransactionSSN = transactionMemberDto.getIdentifiers()
                .stream()
                .filter(
                        transactionMemberIdentifierDto -> transactionMemberIdentifierDto.getIdentifierValue().equals("SSN"))
                .findFirst();
        if(accountMemberDto.getRelationshipTypeCode().equals(transactionMemberDto.getRelationshipTypeCode())){
            // Get the SSN of the member if present in the account
            if(optionalMemberTransactionSSN.isPresent()){
                String transactionSSN = optionalMemberTransactionSSN.get().getIdentifierValue();
                Optional<MemberIdentifierDto> optionalMemberAccountSSN = accountMemberDto.getMemberIdentifiers()
                        .stream()
                        .filter(
                                memberIdentifierDto -> memberIdentifierDto.getIdentifierValue().equals("SSN")
                        ).findFirst();
                if(optionalMemberAccountSSN.isPresent()){
                    String accountSSN = optionalMemberAccountSSN.get().getIdentifierValue();
                    // SSN is present in both the transaction and the account
                    // if they match indicate that the member is a match
                    return accountSSN.equals(transactionSSN);
                }
            }
            // If the SSN does not match, match by first name, last name and date of birth
            // If they all match, indicate that the member is match
            return transactionMemberDto.getFirstName().equals(accountMemberDto.getFirstName()) &&
                    transactionMemberDto.getLastName().equals(accountMemberDto.getLastName()) &&
                    transactionMemberDto.getDateOfBirth().isEqual(accountMemberDto.getDateOfBirth());

        }
        // If the member did not match by relationship type and by either SSN or first, last and DOB then
        // indicate that the member is not a match
        return false;
    }

    /**
     * Identifies if the member entity was updated
     * @param transactionMemberDto The member information in the transaction
     * @param accountMemberDto The member information in the account
     * @return a boolean indicating if the member was updated or not.
     */
    private boolean doMemberChange(TransactionMemberDto transactionMemberDto, MemberDto accountMemberDto){
        boolean memberUpdated = false;
        if(!transactionMemberDto.getFirstName().equals(accountMemberDto.getFirstName())){
            accountMemberDto.setFirstName(transactionMemberDto.getFirstName());
        }
        if(!transactionMemberDto.getMiddleName().equals(accountMemberDto.getMiddleName())){
            accountMemberDto.setMiddleName(transactionMemberDto.getMiddleName());
        }
        return memberUpdated;
    }
}
