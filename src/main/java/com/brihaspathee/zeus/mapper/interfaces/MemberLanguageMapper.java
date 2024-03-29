package com.brihaspathee.zeus.mapper.interfaces;

import com.brihaspathee.zeus.domain.entity.MemberLanguage;
import com.brihaspathee.zeus.dto.account.MemberLanguageDto;

import java.util.List;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 30, November 2022
 * Time: 3:48 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.mapper.interfaces
 * To change this template use File | Settings | File and Code Template
 */
public interface MemberLanguageMapper {

    /**
     * Convert language entity to language dto
     * @param language
     * @return
     */
    MemberLanguageDto languageToLanguageDto(MemberLanguage language);

    /**
     * Convert language dto to language entity
     * @param languageDto
     * @return
     */
    MemberLanguage languageDtoToLanguage(MemberLanguageDto languageDto);

    /**
     *
     * @param languages
     * @return
     */
    List<MemberLanguageDto> languagesToLanguageDtos(List<MemberLanguage> languages);

    /**
     * Convert language dtos to language entities
     * @param languageDtos
     * @return
     */
    List<MemberLanguage> languageDtosToLanguages(List<MemberLanguageDto> languageDtos);
}
