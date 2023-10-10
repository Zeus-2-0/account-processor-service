package com.brihaspathee.zeus.mapper.interfaces;

import com.brihaspathee.zeus.domain.entity.MemberIdentifier;
import com.brihaspathee.zeus.dto.account.MemberIdentifierDto;

import java.util.List;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 30, November 2022
 * Time: 4:20 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.mapper.interfaces
 * To change this template use File | Settings | File and Code Template
 */
public interface MemberIdentifierMapper {

    /**
     * Convert member identifier entity to member identifier dto
     * @param identifier
     * @return
     */
    MemberIdentifierDto identifierToIdentifierDto(MemberIdentifier identifier);

    /**
     * Convert member identifier dto to member identifier entity
     * @param memberIdentifierDto
     * @return
     */
    MemberIdentifier identifierDtoToIdentifier(MemberIdentifierDto memberIdentifierDto);

    /**
     * Convert identifier entities to member identifier dtos
     * @param identifiers
     * @return
     */
    List<MemberIdentifierDto> identifiersToIdentifierDtos(List<MemberIdentifier> identifiers);

    /**
     * Convert identifier dtos to identifier entities
     * @param identifierDtos
     * @return
     */
    List<MemberIdentifier> identifierDtosToIdentifiers(List<MemberIdentifierDto> identifierDtos);
}
