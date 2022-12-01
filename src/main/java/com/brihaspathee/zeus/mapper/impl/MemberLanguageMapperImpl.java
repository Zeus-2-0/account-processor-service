package com.brihaspathee.zeus.mapper.impl;

import com.brihaspathee.zeus.domain.entity.MemberLanguage;
import com.brihaspathee.zeus.dto.account.MemberLanguageDto;
import com.brihaspathee.zeus.mapper.interfaces.MemberLanguageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 30, November 2022
 * Time: 3:51 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.mapper.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberLanguageMapperImpl implements MemberLanguageMapper {

    /**
     * Convert language entity to language dto
     * @param language
     * @return
     */
    @Override
    public MemberLanguageDto languageToLanguageDto(MemberLanguage language) {
        if(language == null){
            return null;
        }
        MemberLanguageDto languageDto = MemberLanguageDto.builder()
                .memberLanguageSK(language.getMemberAcctLangSK())
                .memberLanguageCode(language.getMemberLanguageCode())
                .languageTypeCode(language.getLanguageTypeCode())
                .languageCode(language.getLanguageCode())
                .startDate(language.getStartDate())
                .endDate(language.getEndDate())
                .createdDate(language.getCreatedDate())
                .updatedDate(language.getUpdatedDate())
                .build();
        return languageDto;
    }

    /**
     * Convert language entities to language dtos
     * @param languages
     * @return
     */
    @Override
    public List<MemberLanguageDto> languagesToLanguageDtos(List<MemberLanguage> languages) {
        return languages.stream().map(this::languageToLanguageDto).collect(Collectors.toList());
    }
}
