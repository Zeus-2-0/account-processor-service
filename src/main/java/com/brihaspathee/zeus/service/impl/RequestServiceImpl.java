package com.brihaspathee.zeus.service.impl;

import com.brihaspathee.zeus.domain.entity.ProcessingRequest;
import com.brihaspathee.zeus.domain.repository.ProcessingRequestRepository;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import com.brihaspathee.zeus.service.interfaces.RequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 14, January 2024
 * Time: 8:49 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.service.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    /**
     * Request repository instance
     */
    private final ProcessingRequestRepository requestRepository;

    /**
     * Saves the request received to process the transaction
     * @param transactionDto
     * @return
     */
    @Override
    public ProcessingRequest saveRequest(TransactionDto transactionDto) {
        ProcessingRequest request = ProcessingRequest.builder()
                .zrcnTypeCode("TRANSACTION")
                .zrcn(transactionDto.getZtcn())
                .source(transactionDto.getSource())
                .requestReceivedDate(transactionDto.getTransactionReceivedDate())
                .build();
        return requestRepository.save(request);
    }
}
