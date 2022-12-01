package com.brihaspathee.zeus.mapper.impl;

import com.brihaspathee.zeus.domain.entity.EnrollmentSpan;
import com.brihaspathee.zeus.dto.account.EnrollmentSpanDto;
import com.brihaspathee.zeus.mapper.interfaces.EnrollmentSpanMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 01, December 2022
 * Time: 11:12 AM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.mapper.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EnrollmentSpanMapperImpl implements EnrollmentSpanMapper {

    /**
     * Convert enrollment span entity to enrollment span dto
     * @param enrollmentSpan
     * @return
     */
    @Override
    public EnrollmentSpanDto enrollmentSpanToEnrollmentSpanDto(EnrollmentSpan enrollmentSpan) {
        if(enrollmentSpan == null){
            return null;
        }
        EnrollmentSpanDto enrollmentSpanDto = EnrollmentSpanDto.builder()
                .enrollmentSpanSK(enrollmentSpan.getAcctEnrollmentSpanSK())
                .enrollmentSpanCode(enrollmentSpan.getEnrollmentSpanCode())
                .stateTypeCode(enrollmentSpan.getStateTypeCode())
                .marketplaceTypeCode(enrollmentSpan.getMarketplaceTypeCode())
                .businessUnitTypeCode(enrollmentSpan.getBusinessUnitTypeCode())
                .startDate(enrollmentSpan.getStartDate())
                .endDate(enrollmentSpan.getEndDate())
                .exchangeSubscriberId(enrollmentSpan.getExchangeSubscriberId())
                .planId(enrollmentSpan.getPlanId())
                .groupPolicyId(enrollmentSpan.getGroupPolicyId())
                .planId(enrollmentSpan.getPlanId())
                .productTypeCode(enrollmentSpan.getProductTypeCode())
                .statusTypeCode(enrollmentSpan.getStatusTypeCode())
                .createdDate(enrollmentSpan.getCreatedDate())
                .updatedDate(enrollmentSpan.getUpdatedDate())
                .build();
        return enrollmentSpanDto;
    }

    /**
     * Convert enrollment span entities to enrollment span dtos
     * @param enrollmentSpans
     * @return
     */
    @Override
    public List<EnrollmentSpanDto> enrollmentSpansToEnrollmentSpanDtos(List<EnrollmentSpan> enrollmentSpans) {
        return enrollmentSpans.stream().map(this::enrollmentSpanToEnrollmentSpanDto).collect(Collectors.toList());
    }
}
