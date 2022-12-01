package com.brihaspathee.zeus.mapper.interfaces;

import com.brihaspathee.zeus.domain.entity.EnrollmentSpan;
import com.brihaspathee.zeus.dto.account.EnrollmentSpanDto;

import java.util.List;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 01, December 2022
 * Time: 11:09 AM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.mapper.interfaces
 * To change this template use File | Settings | File and Code Template
 */
public interface EnrollmentSpanMapper {

    /**
     * Convert enrollment span entity to enrollment span dto
     * @param enrollmentSpan
     * @return
     */
    EnrollmentSpanDto enrollmentSpanToEnrollmentSpanDto(EnrollmentSpan enrollmentSpan);

    /**
     * Convert enrollment span entities to enrollment span dtos
     * @param enrollmentSpans
     * @return
     */
    List<EnrollmentSpanDto> enrollmentSpansToEnrollmentSpanDtos(List<EnrollmentSpan> enrollmentSpans);
}
