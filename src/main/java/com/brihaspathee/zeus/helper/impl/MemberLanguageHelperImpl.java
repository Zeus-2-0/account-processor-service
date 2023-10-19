package com.brihaspathee.zeus.helper.impl;

import com.brihaspathee.zeus.domain.entity.Member;
import com.brihaspathee.zeus.domain.entity.MemberLanguage;
import com.brihaspathee.zeus.domain.repository.MemberLanguageRepository;
import com.brihaspathee.zeus.dto.account.MemberDto;
import com.brihaspathee.zeus.dto.account.MemberLanguageDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberLanguageDto;
import com.brihaspathee.zeus.helper.interfaces.MemberLanguageHelper;
import com.brihaspathee.zeus.mapper.interfaces.MemberLanguageMapper;
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
public class MemberLanguageHelperImpl implements MemberLanguageHelper {

    /**
     * Member language mapper instance
     */
    private final MemberLanguageMapper languageMapper;

    /**
     * Member language repository instance to perform CRUD operations
     */
    private final MemberLanguageRepository memberLanguageRepository;

    /**
     * The utility class for account processor service
     */
    private final AccountProcessorUtil accountProcessorUtil;

    /**
     * Create the member language
     * @param member
     * @param transactionMemberDto
     */
    @Override
    public void createMemberLanguage(Member member, TransactionMemberDto transactionMemberDto) {
        if(transactionMemberDto.getLanguages() != null && transactionMemberDto.getLanguages().size() > 0){
            List<MemberLanguage> languages = new ArrayList<>();
            transactionMemberDto.getLanguages().forEach(languageDto -> {
                String memberLanguageCode = accountProcessorUtil.generateUniqueCode(transactionMemberDto.getEntityCodes(),
                        "memberLanguageCode");
                MemberLanguage memberLanguage = MemberLanguage.builder()
                        .member(member)
                        .memberAcctLangSK(null)
                        .memberLanguageCode(memberLanguageCode)
                        .languageTypeCode(languageDto.getLanguageTypeCode())
                        .languageCode(languageDto.getLanguageCode())
                        .startDate(languageDto.getReceivedDate().toLocalDate())
                        .endDate(null)
                        .changed(true)
                        .build();
                memberLanguage = memberLanguageRepository.save(memberLanguage);
                languages.add(memberLanguage);
            });
            member.setMemberLanguages(languages);
        }
    }

    /**
     * Create the member language in the repository
     * @param member
     * @param transactionMemberLanguageDto
     * @return
     */
    private MemberLanguage createMemberLanguage(Member member,
                                                TransactionMemberLanguageDto transactionMemberLanguageDto,
                                                String memberLanguageCode){
        MemberLanguage memberLanguage = MemberLanguage.builder()
                .member(member)
                .memberAcctLangSK(null)
                .memberLanguageCode(memberLanguageCode)
                .languageTypeCode(transactionMemberLanguageDto.getLanguageTypeCode())
                .languageCode(transactionMemberLanguageDto.getLanguageCode())
                .startDate(transactionMemberLanguageDto.getReceivedDate().toLocalDate())
                .endDate(null)
                .changed(true)
                .build();
        memberLanguage = memberLanguageRepository.save(memberLanguage);
        return memberLanguage;
    }

    /**
     * Set the member language dto to  send to MMS
     * @param memberDto
     * @param member
     */
    @Override
    public void setMemberLanguage(MemberDto memberDto, Member member) {
        if(member.getMemberLanguages() != null && member.getMemberLanguages().size() >0){
            memberDto.setMemberLanguages(
                    languageMapper
                            .languagesToLanguageDtos(
                                    member.getMemberLanguages())
                            .stream()
                            .collect(Collectors.toSet())
            );
        }
    }

    /**
     * Match member language from the account to the language in the transaction
     * @param member
     * @param memberDto
     * @param transactionMemberDto
     */
    @Override
    public void matchMemberLanguage(Member member, MemberDto memberDto, TransactionMemberDto transactionMemberDto) {
        log.info("Inside member match language");
        // Check if the transaction has any languages for the member
        // If there are no languages in the transaction then return
        if(transactionMemberDto.getLanguages() == null ||
            transactionMemberDto.getLanguages().isEmpty()){
            return;
        }
        // There are four types of languages possible "SPEAKING" and "WRITTEN"
        matchMemberLanguage("SPEAKING", member, memberDto, transactionMemberDto);
        matchMemberLanguage("WRITTEN", member, memberDto, transactionMemberDto);
        matchMemberLanguage("READING", member, memberDto, transactionMemberDto);
        matchMemberLanguage("NATIVE", member, memberDto, transactionMemberDto);
    }

    /**
     * Match the specific language type
     * @param languageTypeCode
     * @param member
     * @param memberDto
     * @param transactionMemberDto
     */
    private void matchMemberLanguage(String languageTypeCode, Member member, MemberDto memberDto, TransactionMemberDto transactionMemberDto){
        List<MemberLanguage> languages = new ArrayList<>();
        // Check if the transactions has the passed in language type for the member
        Optional<TransactionMemberLanguageDto> optionalTransactionLanguageDto = transactionMemberDto.getLanguages()
                .stream()
                .filter(
                        transactionMemberLanguageDto -> transactionMemberLanguageDto.getLanguageTypeCode().equals(languageTypeCode)
                ).findFirst();
        // If there are no languages of the passed type, then return - nothing to do compare here
        if(optionalTransactionLanguageDto.isEmpty()){
            return;
        }
        // If the control reaches here, then the transaction contains the language of the passed type
        TransactionMemberLanguageDto transactionLanguageDto = optionalTransactionLanguageDto.get();
        // See if the account has the same language type
        Optional<MemberLanguageDto> optionalAccountLanguageDto = memberDto.getMemberLanguages()
                .stream().filter(
                        languageDto ->
                                languageDto.getLanguageTypeCode().equals(languageTypeCode) &&
                                        languageDto.getEndDate() == null
                ).findFirst();
        if (optionalAccountLanguageDto.isEmpty()){
            // this means that the account does not have language
            // create the language received in the transaction
            // Since this will be a new language create the language code
            String memberLanguageCode = accountProcessorUtil.generateUniqueCode(transactionMemberDto.getEntityCodes(),
                    "memberLanguageCode");
            MemberLanguage memberLanguage = createMemberLanguage(member,
                    transactionLanguageDto,
                    memberLanguageCode);
            languages.add(memberLanguage);
            return;
        }
        // if the control reaches here, then the transaction and the account has the same language
        MemberLanguageDto accountLanguageDto = optionalAccountLanguageDto.get();
        // compare the languages
        if (!transactionLanguageDto.getLanguageCode().equals(
                accountLanguageDto.getLanguageCode())){
            // the language codes are different
            // create the language as received in the transaction
            String memberLanguageCode = accountProcessorUtil.generateUniqueCode(transactionMemberDto.getEntityCodes(),
                    "memberLanguageCode");
            MemberLanguage memberLanguage = createMemberLanguage(member,
                    transactionLanguageDto,
                    memberLanguageCode);
            languages.add(memberLanguage);
            // set the end date of the language in the account to one day prior to the
            // transaction received date
            accountLanguageDto.setEndDate(transactionLanguageDto.getReceivedDate().minusDays(1).toLocalDate());
            MemberLanguage updatedLanguage = languageMapper.languageDtoToLanguage(accountLanguageDto);
            // set the language sk of the language in MMS
            updatedLanguage.setMemberAcctLangSK(accountLanguageDto.getMemberLanguageSK());
            // Set the changed flag to true
            updatedLanguage.setChanged(true);
            // save the language to the repository
            updatedLanguage = memberLanguageRepository.save(updatedLanguage);
            // add the language to the list
            languages.add(updatedLanguage);
        }
        if(member.getMemberLanguages() == null || member.getMemberLanguages().isEmpty()){
            member.setMemberLanguages(languages);
        }else{
            member.getMemberLanguages().addAll(languages);
        }
    }
}
