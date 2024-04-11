package com.brihaspathee.zeus.mapper.impl;

import com.brihaspathee.zeus.domain.entity.ProcessingRequest;
import com.brihaspathee.zeus.mapper.interfaces.ProcessingRequestMapper;
import com.brihaspathee.zeus.web.model.ProcessingRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 07, April 2024
 * Time: 7:19 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.mapper.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessingRequestMapperImpl implements ProcessingRequestMapper {

    /**
     * Convert processing request entity to processing request dto
     * @param processingRequest
     * @return
     */
    @Override
    public ProcessingRequestDto processingRequestToProcessingRequestDto(ProcessingRequest processingRequest) {
        if (processingRequest == null){
            return null;
        }
        ProcessingRequestDto processingRequestDto = ProcessingRequestDto.builder()
                .zrcnTypeCode(processingRequest.getZrcnTypeCode())
                .zrcn(processingRequest.getZrcn())
                .source(processingRequest.getSource())
                .requestPayloadId(processingRequest.getRequestPayloadId())
                .build();
        return processingRequestDto;
    }
}
