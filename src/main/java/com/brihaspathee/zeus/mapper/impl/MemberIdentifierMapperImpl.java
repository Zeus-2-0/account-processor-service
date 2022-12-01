package com.brihaspathee.zeus.mapper.impl;

import com.brihaspathee.zeus.domain.entity.MemberIdentifier;
import com.brihaspathee.zeus.dto.account.MemberIdentifierDto;
import com.brihaspathee.zeus.mapper.interfaces.MemberIdentifierMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 30, November 2022
 * Time: 4:23 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.mapper.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberIdentifierMapperImpl implements MemberIdentifierMapper {

    /**
     * Convert member identifier entity to member identifier dto
     * @param identifier
     * @return
     */
    @Override
    public MemberIdentifierDto identifierToIdentifierDto(MemberIdentifier identifier) {
        if(identifier == null){
            return null;
        }
        MemberIdentifierDto identifierDto = MemberIdentifierDto.builder()
                .memberIdentifierSK(identifier.getMemberAcctIdentifierSK())
                .memberIdentifierCode(identifier.getMemberIdentifierCode())
                .identifierTypeCode(identifier.getIdentifierTypeCode())
                .identifierValue(identifier.getIdentifierValue())
                .isActive(identifier.isActive())
                .createdDate(identifier.getCreatedDate())
                .updatedDate(identifier.getUpdatedDate())
                .build();
        return identifierDto;
    }

    /**
     * Convert member identifier entities to member identifier dtos
     * @param identifiers
     * @return
     */
    @Override
    public List<MemberIdentifierDto> identifiersToIdentifierDtos(List<MemberIdentifier> identifiers) {
        return identifiers.stream().map(this::identifierToIdentifierDto).collect(Collectors.toList());
    }
}
