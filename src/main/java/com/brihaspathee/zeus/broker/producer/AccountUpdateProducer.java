package com.brihaspathee.zeus.broker.producer;

import com.brihaspathee.zeus.constants.ZeusServiceNames;
import com.brihaspathee.zeus.domain.entity.PayloadTracker;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.helper.interfaces.PayloadTrackerHelper;
import com.brihaspathee.zeus.message.MessageMetadata;
import com.brihaspathee.zeus.message.ZeusMessagePayload;
import com.brihaspathee.zeus.util.ZeusRandomStringGenerator;
import com.brihaspathee.zeus.broker.message.AccountUpdateRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 05, December 2022
 * Time: 2:48 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.broker.producer
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountUpdateProducer {

    /**
     * The kafka template to send the message to MMS
     */
    private final KafkaTemplate<String, ZeusMessagePayload<AccountUpdateRequest>> kafkaTemplate;

    /**
     * The call back instance that is invoked after the message was published or failed to publish
     */
    private final AccountUpdateCallback accountUpdateCallback;

    /**
     * Object mapper to convert the payload to string
     */
    private final ObjectMapper objectMapper;

    /**
     * Payload tracker helper instance to create the payload tracker record
     */
    private final PayloadTrackerHelper payloadTrackerHelper;

    /**
     * The method that publishes the messages to the kafka topic
     * @param accountUpdateRequest
     * @param parentPayloadId
     */
    public void updateAccount(AccountUpdateRequest accountUpdateRequest, String parentPayloadId) throws JsonProcessingException {
        log.info("About to publish the account to MMS;{}", accountUpdateRequest.getAccountDto().getAccountNumber());
        String[] messageDestinations = {ZeusServiceNames.MEMBER_MGMT_SERVICE};
        ZeusMessagePayload<AccountUpdateRequest> messagePayload = ZeusMessagePayload.<AccountUpdateRequest>builder()
                .messageMetadata(MessageMetadata.builder()
                        .messageSource(ZeusServiceNames.ACCOUNT_PROCESSOR_SERVICE)
                        .messageDestination(messageDestinations)
                        .messageCreationTimestamp(LocalDateTime.now())
                        .build())
                .payload(accountUpdateRequest)
                .payloadId(ZeusRandomStringGenerator.randomString(15))
                .build();
        accountUpdateCallback.setAccountUpdateRequest(accountUpdateRequest);
        PayloadTracker payloadTracker = createPayloadTracker(messagePayload, parentPayloadId);
        log.info("Payload tracker created to send the account {} to account processor service is {}",
                accountUpdateRequest.getAccountDto().getAccountNumber(),
                payloadTracker.getPayloadId());
        ProducerRecord<String, ZeusMessagePayload<AccountUpdateRequest>> producerRecord =
                buildProducerRecord(messagePayload);
        kafkaTemplate.send(producerRecord);//.addCallback(accountUpdateCallback);
        log.info("After the publishing the account {} to MMS",
                accountUpdateRequest.getAccountDto().getAccountNumber());
    }

    /**
     * The method to build the producer record
     * @param messagePayload
     */
    private ProducerRecord<String, ZeusMessagePayload<AccountUpdateRequest>> buildProducerRecord(
            ZeusMessagePayload<AccountUpdateRequest> messagePayload){
        RecordHeader messageHeader = new RecordHeader("payload-id",
                "test payload id".getBytes());
        return new ProducerRecord<>("ZEUS.ACCOUNT.UPDATE.REQ",
                null,
                "test payload id 2",
                messagePayload,
                Arrays.asList(messageHeader));
    }

    /**
     * Create the payload tracker record
     * @param messagePayload
     * @param parentPayloadId
     * @throws JsonProcessingException
     */
    private PayloadTracker createPayloadTracker(ZeusMessagePayload<AccountUpdateRequest> messagePayload,
                                                String parentPayloadId)
            throws JsonProcessingException {
        String payloadAsString = objectMapper.writeValueAsString(messagePayload);
        PayloadTracker payloadTracker = PayloadTracker.builder()
                .payloadDirectionTypeCode("OUTBOUND")
                .payload_key(messagePayload.getPayload().getAccountDto().getAccountNumber())
                .payload_key_type_code("ACCOUNT")
                .payload(payloadAsString)
                .payloadId(messagePayload.getPayloadId())
                .parentPayloadId(parentPayloadId)
                .sourceDestinations(StringUtils.join(
                        messagePayload.getMessageMetadata().getMessageDestination()))
                .build();
        return payloadTrackerHelper.createPayloadTracker(payloadTracker);
    }

}
