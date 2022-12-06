package com.brihaspathee.zeus.broker.producer;

import com.brihaspathee.zeus.message.ZeusMessagePayload;
import com.brihaspathee.zeus.broker.message.AccountUpdateRequest;
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
 * Date: 05, December 2022
 * Time: 2:52 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.broker.producer
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Getter
@Setter
@Component
@RequiredArgsConstructor
public class AccountUpdateCallback implements
        ListenableFutureCallback<SendResult<String, ZeusMessagePayload<AccountUpdateRequest>>> {

    /**
     * The message that was sent in the Kafka topic
     */
    private AccountUpdateRequest accountUpdateRequest;

    /**
     * Invoked when there was a failure to post the message to Kafka topic
     * @param ex
     */
    @Override
    public void onFailure(Throwable ex) {
        log.info("The message from APS to MMS failed to publish");
    }

    /**
     * Invoked after the message was successfully posted in the kafka topic
     * @param result
     */
    @Override
    public void onSuccess(SendResult<String, ZeusMessagePayload<AccountUpdateRequest>> result) {
        log.info("The message from APS to MMS was successfully published");
    }
}
