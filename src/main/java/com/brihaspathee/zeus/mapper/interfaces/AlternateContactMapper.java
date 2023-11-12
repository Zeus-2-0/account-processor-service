package com.brihaspathee.zeus.mapper.interfaces;

import com.brihaspathee.zeus.domain.entity.AlternateContact;
import com.brihaspathee.zeus.dto.account.AlternateContactDto;

import java.util.List;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 30, November 2022
 * Time: 1:38 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.mapper.interfaces
 * To change this template use File | Settings | File and Code Template
 */
public interface AlternateContactMapper {

    /**
     * Convert alternate contact entity to alternate contact dto
     * @param alternateContact
     * @return
     */
    AlternateContactDto alternateContactToAlternateContactDto(AlternateContact alternateContact);

    /**
     * Convert alternate contact dto to alternate contact entity
     * @param alternateContactDto
     * @return
     */
    AlternateContact alternateContactDtoToAlternateContact(AlternateContactDto alternateContactDto);

    /**
     * Convert alternate contact entities to alternate contact dtos
     * @param alternateContacts
     * @return
     */
    List<AlternateContactDto> alternateContactsToAlternateContactDtos(List<AlternateContact> alternateContacts);

    /**
     * Convert the alternate contact dtos to alternate contact entities
     * @param alternateContactDtos
     * @return
     */
    List<AlternateContact> alternateContactDtosToAlternateContacts(List<AlternateContactDto> alternateContactDtos);
}
