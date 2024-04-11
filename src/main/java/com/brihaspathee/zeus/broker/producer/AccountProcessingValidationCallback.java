package com.brihaspathee.zeus.broker.producer;

import com.brihaspathee.zeus.message.ZeusMessagePayload;
import com.brihaspathee.zeus.validator.request.ProcessingValidationRequest;
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
 * Date: 03, April 2024
 * Time: 9:32 AM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.broker.producer
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Getter
@Setter
@Component
@RequiredArgsConstructor
public class AccountProcessingValidationCallback implements
        ListenableFutureCallback<SendResult<String, ZeusMessagePayload<ProcessingValidationRequest>>> {

    /**
     * The payload that is sent to the validation service
     */
    private ProcessingValidationRequest accountProcessingValidationRequest;

    /**
     * This is invoked when there was a failure to publish the message
     * @param ex
     */
    @Override
    public void onFailure(Throwable ex) {
        log.info("The message failed to publish to the validation service");
    }

    @Override
    public void onSuccess(SendResult<String, ZeusMessagePayload<ProcessingValidationRequest>> result) {
        log.info("The message successfully published to Validation Service");
    }
}
