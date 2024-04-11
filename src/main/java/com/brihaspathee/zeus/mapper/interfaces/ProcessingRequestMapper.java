package com.brihaspathee.zeus.mapper.interfaces;

import com.brihaspathee.zeus.domain.entity.ProcessingRequest;
import com.brihaspathee.zeus.web.model.ProcessingRequestDto;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 07, April 2024
 * Time: 7:18 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.mapper.interfaces
 * To change this template use File | Settings | File and Code Template
 */
public interface ProcessingRequestMapper {

    /**
     *
     * @param processingRequest
     * @return
     */
    ProcessingRequestDto processingRequestToProcessingRequestDto(ProcessingRequest processingRequest);

}
