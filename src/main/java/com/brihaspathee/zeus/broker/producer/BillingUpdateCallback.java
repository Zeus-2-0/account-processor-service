package com.brihaspathee.zeus.broker.producer;

import com.brihaspathee.zeus.broker.message.request.BillingUpdateRequest;
import com.brihaspathee.zeus.message.ZeusMessagePayload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 21, April 2024
 * Time: 7:44 AM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.broker.producer
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Getter
@Setter
@Component
@RequiredArgsConstructor
public class BillingUpdateCallback implements
        ListenableFutureCallback<SendResult<String, ZeusMessagePayload<BillingUpdateRequest>>> {

    /**
     * The payload that is sent to premium billing service
     */
    private BillingUpdateRequest billingUpdateRequest;

    /**
     * This method is invoked when there is failure to publish the message
     * @param ex
     */
    @Override
    public void onFailure(Throwable ex) {
        log.info("The message failed to publish to the premium billing service");
    }

    /**
     * This method is invoked when the message is published successfully
     * @param result
     */
    @Override
    public void onSuccess(SendResult<String, ZeusMessagePayload<BillingUpdateRequest>> result) {
        log.info("The message was successfully published to the premium billing service");
    }
}
