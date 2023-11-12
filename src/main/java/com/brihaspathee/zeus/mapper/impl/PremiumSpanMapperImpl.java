package com.brihaspathee.zeus.mapper.impl;

import com.brihaspathee.zeus.domain.entity.PremiumSpan;
import com.brihaspathee.zeus.dto.account.PremiumSpanDto;
import com.brihaspathee.zeus.mapper.interfaces.PremiumSpanMapper;
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
 * Time: 12:08 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.mapper.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PremiumSpanMapperImpl implements PremiumSpanMapper {

    /**
     * Convert Premium span entity to premium span dto
     * @param premiumSpan
     * @return
     */
    @Override
    public PremiumSpanDto premiumSpanToPremiumSpanDto(PremiumSpan premiumSpan) {
        if(premiumSpan == null){
            return null;
        }
        PremiumSpanDto premiumSpanDto = PremiumSpanDto.builder()
                .premiumSpanSK(premiumSpan.getPremiumSpanSK())
                .premiumSpanCode(premiumSpan.getPremiumSpanCode())
                .ztcn(premiumSpan.getZtcn())
                .startDate(premiumSpan.getStartDate())
                .endDate(premiumSpan.getEndDate())
                .statusTypeCode(premiumSpan.getStatusTypeCode())
                .csrVariant(premiumSpan.getCsrVariant())
                .totalPremiumAmount(premiumSpan.getTotalPremAmount())
                .totalResponsibleAmount(premiumSpan.getTotalResponsibleAmount())
                .aptcAmount(premiumSpan.getAptcAmount())
                .otherPayAmount(premiumSpan.getOtherPayAmount())
                .csrAmount(premiumSpan.getCsrAmount())
                .changed(new AtomicBoolean(premiumSpan.isChanged()))
                .createdDate(premiumSpan.getCreatedDate())
                .updatedDate(premiumSpan.getUpdatedDate())
                .build();
        return premiumSpanDto;
    }

    @Override
    public PremiumSpan premiumSpanDtoToPremiumSpan(PremiumSpanDto premiumSpanDto) {
        if(premiumSpanDto == null){
            return null;
        }
        PremiumSpan premiumSpan = PremiumSpan.builder()
                .premiumSpanCode(premiumSpanDto.getPremiumSpanCode())
                .ztcn(premiumSpanDto.getZtcn())
                .startDate(premiumSpanDto.getStartDate())
                .endDate(premiumSpanDto.getEndDate())
                .statusTypeCode(premiumSpanDto.getStatusTypeCode())
                .csrVariant(premiumSpanDto.getCsrVariant())
                .totalPremAmount(premiumSpanDto.getTotalPremiumAmount())
                .totalResponsibleAmount(premiumSpanDto.getTotalResponsibleAmount())
                .aptcAmount(premiumSpanDto.getAptcAmount())
                .otherPayAmount(premiumSpanDto.getOtherPayAmount())
                .csrAmount(premiumSpanDto.getCsrAmount())
                .createdDate(premiumSpanDto.getCreatedDate())
                .updatedDate(premiumSpanDto.getUpdatedDate())
                .build();
        if (premiumSpanDto.getChanged() != null){
            premiumSpan.setChanged(premiumSpanDto.getChanged().get());
        } else {
            premiumSpan.setChanged(false);
        }
        return premiumSpan;
    }

    /**
     * Convert premium span entities to premium span dtos
     * @param premiumSpans
     * @return
     */
    @Override
    public List<PremiumSpanDto> premiumSpanToPremiumSpanDtos(List<PremiumSpan> premiumSpans) {
        return premiumSpans.stream().map(this::premiumSpanToPremiumSpanDto).collect(Collectors.toList());
    }

    @Override
    public List<PremiumSpan> premiumSpanDtosToPremiumSpans(List<PremiumSpanDto> premiumSpanDtos) {
        return premiumSpanDtos.stream().map(this::premiumSpanDtoToPremiumSpan).collect(Collectors.toList());
    }
}
