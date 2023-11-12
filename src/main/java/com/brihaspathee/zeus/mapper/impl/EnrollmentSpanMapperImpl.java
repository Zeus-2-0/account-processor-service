package com.brihaspathee.zeus.mapper.impl;

import com.brihaspathee.zeus.domain.entity.EnrollmentSpan;
import com.brihaspathee.zeus.dto.account.EnrollmentSpanDto;
import com.brihaspathee.zeus.mapper.interfaces.EnrollmentSpanMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
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
                .ztcn(enrollmentSpan.getZtcn())
                .stateTypeCode(enrollmentSpan.getStateTypeCode())
                .marketplaceTypeCode(enrollmentSpan.getMarketplaceTypeCode())
                .businessUnitTypeCode(enrollmentSpan.getBusinessUnitTypeCode())
                .coverageTypeCode(enrollmentSpan.getCoverageTypeCode())
                .startDate(enrollmentSpan.getStartDate())
                .endDate(enrollmentSpan.getEndDate())
                .effectuationDate(enrollmentSpan.getEffectuationDate())
                .exchangeSubscriberId(enrollmentSpan.getExchangeSubscriberId())
                .planId(enrollmentSpan.getPlanId())
                .groupPolicyId(enrollmentSpan.getGroupPolicyId())
                .planId(enrollmentSpan.getPlanId())
                .productTypeCode(enrollmentSpan.getProductTypeCode())
                .delinqInd(enrollmentSpan.isDelinqInd())
                .paidThroughDate(enrollmentSpan.getPaidThroughDate())
                .claimPaidThroughDate(enrollmentSpan.getClaimPaidThroughDate())
                .statusTypeCode(enrollmentSpan.getStatusTypeCode())
                .changed(new AtomicBoolean(enrollmentSpan.isChanged()))
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

    /**
     * Convert enrollment span dto to enrollment span mapper
     * @param enrollmentSpanDto
     * @return
     */
    @Override
    public EnrollmentSpan enrollmentSpanDtoToEnrollmentSpan(EnrollmentSpanDto enrollmentSpanDto) {
        if(enrollmentSpanDto == null){
            return null;
        }
        EnrollmentSpan enrollmentSpan = EnrollmentSpan.builder()
                .enrollmentSpanCode(enrollmentSpanDto.getEnrollmentSpanCode())
                .ztcn(enrollmentSpanDto.getZtcn())
                .stateTypeCode(enrollmentSpanDto.getStateTypeCode())
                .marketplaceTypeCode(enrollmentSpanDto.getMarketplaceTypeCode())
                .businessUnitTypeCode(enrollmentSpanDto.getBusinessUnitTypeCode())
                .coverageTypeCode(enrollmentSpanDto.getCoverageTypeCode())
                .startDate(enrollmentSpanDto.getStartDate())
                .endDate(enrollmentSpanDto.getEndDate())
                .effectuationDate(enrollmentSpanDto.getEffectuationDate())
                .exchangeSubscriberId(enrollmentSpanDto.getExchangeSubscriberId())
                .planId(enrollmentSpanDto.getPlanId())
                .groupPolicyId(enrollmentSpanDto.getGroupPolicyId())
                .planId(enrollmentSpanDto.getPlanId())
                .productTypeCode(enrollmentSpanDto.getProductTypeCode())
                .delinqInd(enrollmentSpanDto.isDelinqInd())
                .paidThroughDate(enrollmentSpanDto.getPaidThroughDate())
                .claimPaidThroughDate(enrollmentSpanDto.getClaimPaidThroughDate())
                .statusTypeCode(enrollmentSpanDto.getStatusTypeCode())
                .createdDate(enrollmentSpanDto.getCreatedDate())
                .updatedDate(enrollmentSpanDto.getUpdatedDate())
                .build();
        if(enrollmentSpanDto.getChanged() != null){
            enrollmentSpanDto.setChanged(enrollmentSpanDto.getChanged());
        } else {
            enrollmentSpanDto.setChanged(new AtomicBoolean(false));
        }
        return enrollmentSpan;
    }

    /**
     * Convert enrollment span dtos to enrollment span entities
     * @param enrollmentSpanDtos
     * @return
     */
    @Override
    public List<EnrollmentSpan> enrollmentSpanDtosToEnrollmentSpan(List<EnrollmentSpanDto> enrollmentSpanDtos) {
        return enrollmentSpanDtos.stream().map(this::enrollmentSpanDtoToEnrollmentSpan).collect(Collectors.toList());
    }
}
