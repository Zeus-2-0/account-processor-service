package com.brihaspathee.zeus.mapper.interfaces;

import com.brihaspathee.zeus.domain.entity.Sponsor;
import com.brihaspathee.zeus.dto.account.SponsorDto;

import java.util.List;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 30, November 2022
 * Time: 12:06 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.mapper.interfaces
 * To change this template use File | Settings | File and Code Template
 */
public interface SponsorMapper {

    /**
     * Convert the sponsor entity to sponsor dto
     * @param sponsor
     * @return
     */
    SponsorDto sponsorToSponsorDto(Sponsor sponsor);

    /**
     * Convert the sponsor entities to sponsor dtos
     * @param sponsors
     * @return
     */
    List<SponsorDto> sponsorsToSponsorDtos(List<Sponsor> sponsors);
}
