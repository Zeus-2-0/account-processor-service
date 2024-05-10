package com.brihaspathee.zeus.service.interfaces;

import com.brihaspathee.zeus.domain.entity.ProcessingRequest;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;

import java.util.Optional;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 14, January 2024
 * Time: 8:48 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.service.interfaces
 * To change this template use File | Settings | File and Code Template
 */
public interface RequestService {

    /**
     * Save the request
     * @param transactionDto
     * @param requestPayloadId
     * @return
     */
    ProcessingRequest saveRequest(TransactionDto transactionDto, String requestPayloadId);

    /**
     * Find processing request by payload id
     * @param requestPayloadId
     * @return
     */
    ProcessingRequest getProcessingRequest(String requestPayloadId);

    /**
     * Delete data by ZRCN
     */
    void deleteByZrcn(String zrcn);

    /**
     * Clean up the entire database
     */
    void deleteAll();
}
