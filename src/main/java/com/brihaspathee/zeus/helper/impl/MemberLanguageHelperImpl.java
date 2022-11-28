package com.brihaspathee.zeus.helper.impl;

import com.brihaspathee.zeus.domain.entity.Member;
import com.brihaspathee.zeus.domain.entity.MemberLanguage;
import com.brihaspathee.zeus.domain.repository.MemberLanguageRepository;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberDto;
import com.brihaspathee.zeus.helper.interfaces.MemberLanguageHelper;
import com.brihaspathee.zeus.util.ZeusRandomStringGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
     * Member language repository instance to perform CRUD operations
     */
    private final MemberLanguageRepository memberLanguageRepository;

    @Override
    public void createMemberLanguage(Member member, TransactionMemberDto transactionMemberDto) {
        if(transactionMemberDto.getLanguages() != null && transactionMemberDto.getLanguages().size() > 0){
            List<MemberLanguage> languages = new ArrayList<>();
            transactionMemberDto.getLanguages().stream().forEach(languageDto -> {
                MemberLanguage memberLanguage = MemberLanguage.builder()
                        .member(member)
                        .memberAcctLangSK(null)
                        .memberLanguageCode(ZeusRandomStringGenerator.randomString(15))
                        .languageTypeCode(languageDto.getLanguageTypeCode())
                        .languageCode(languageDto.getLanguageCode())
                        .startDate(languageDto.getReceivedDate().toLocalDate())
                        .endDate(null)
                        .build();
                memberLanguage = memberLanguageRepository.save(memberLanguage);
                languages.add(memberLanguage);
            });
            member.setMemberLanguages(languages);
        }
    }
}
