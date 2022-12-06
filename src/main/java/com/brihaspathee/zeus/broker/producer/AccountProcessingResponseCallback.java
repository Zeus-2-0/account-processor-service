package com.brihaspathee.zeus.broker.producer;

import com.brihaspathee.zeus.broker.message.AccountProcessingResponse;
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
 * Date: 06, December 2022
 * Time: 10:52 AM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.broker.producer
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Getter
@Setter
@Component
@RequiredArgsConstructor
public class AccountProcessingResponseCallback implements
        ListenableFutureCallback<SendResult<String, ZeusMessagePayload<AccountProcessingResponse>>> {

    /**
     * The payload that is sent to the transaction manager service
     */
    private AccountProcessingResponse accountProcessingResponse;

    /**
     * This is invoked when there was a failure to publish the message
     * @param ex
     */
    @Override
    public void onFailure(Throwable ex) {
        log.info("The message failed to publish to TMS");
    }

    @Override
    public void onSuccess(SendResult<String, ZeusMessagePayload<AccountProcessingResponse>> result) {
        log.info("The message successfully published to TMS");
    }
}
