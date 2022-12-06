package com.brihaspathee.zeus.helper.interfaces;

import com.brihaspathee.zeus.domain.entity.PayloadTracker;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 02, December 2022
 * Time: 9:54 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.helper.interfaces
 * To change this template use File | Settings | File and Code Template
 */
public interface PayloadTrackerHelper {

    /**
     * Create the payload tracker record
     * @param payloadTracker
     * @return
     */
    PayloadTracker createPayloadTracker(PayloadTracker payloadTracker);

    /**
     * Get payload tracker by payload id
     * @param payloadId
     * @return
     */
    PayloadTracker getPayloadTracker(String payloadId);
}
