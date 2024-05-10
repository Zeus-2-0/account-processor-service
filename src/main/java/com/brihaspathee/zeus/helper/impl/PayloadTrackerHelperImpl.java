package com.brihaspathee.zeus.helper.impl;

import com.brihaspathee.zeus.domain.entity.PayloadTracker;
import com.brihaspathee.zeus.domain.repository.PayloadTrackerRepository;
import com.brihaspathee.zeus.helper.interfaces.PayloadTrackerHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 02, December 2022
 * Time: 9:57 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.helper.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayloadTrackerHelperImpl implements PayloadTrackerHelper {

    /**
     * The repository instance to perform crud operations
     */
    private final PayloadTrackerRepository payloadTrackerRepository;

    /**
     * Create the payload tracker record
     * @param payloadTracker
     * @return
     */
    @Override
    public PayloadTracker createPayloadTracker(PayloadTracker payloadTracker) {
        log.info("Payload tracker about to be inserted: {}", payloadTracker);
        log.info("Payload tracker id to be inserted: {}", payloadTracker.getPayloadId());
        PayloadTracker payloadTracker1 = payloadTrackerRepository.save(payloadTracker);
        log.info("Payload tracker that was inserted: {}", payloadTracker1);
        return payloadTracker1;
    }

    /**
     * Get payload tracker by payload id
     * @param payloadId
     * @return
     */
    @Override
    public PayloadTracker getPayloadTracker(String payloadId) {
        log.info("Request Payload id:{}", payloadId);
        return payloadTrackerRepository.findPayloadTrackerByPayloadId(payloadId).orElseThrow();
    }
}
