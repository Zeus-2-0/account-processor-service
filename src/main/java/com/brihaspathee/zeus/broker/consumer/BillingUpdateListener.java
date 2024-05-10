package com.brihaspathee.zeus.broker.consumer;

import com.brihaspathee.zeus.broker.message.response.BillingUpdateResponse;
import com.brihaspathee.zeus.constants.ZeusTopics;
import com.brihaspathee.zeus.domain.entity.PayloadTrackerDetail;
import com.brihaspathee.zeus.helper.interfaces.PayloadTrackerDetailHelper;
import com.brihaspathee.zeus.helper.interfaces.PayloadTrackerHelper;
import com.brihaspathee.zeus.message.Acknowledgement;
import com.brihaspathee.zeus.message.ZeusMessagePayload;
import com.brihaspathee.zeus.service.interfaces.TransactionProcessor;
import com.brihaspathee.zeus.validator.result.ProcessingValidationResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 24, April 2024
 * Time: 10:48 AM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.broker.consumer
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillingUpdateListener {

    /**
     * Object mapper instance to convert the JSON to object
     */
    private final ObjectMapper objectMapper;

    /**
     * To perform operations on the payload tracker
     */
    private final PayloadTrackerHelper payloadTrackerHelper;

    /**
     * To perform operations on the payload tracker detail
     */
    private final PayloadTrackerDetailHelper payloadTrackerDetailHelper;

    /**
     * Transaction processor instance to continue processing the transaction
     */
    private final TransactionProcessor transactionProcessor;

    /**
     * kafka consumer to consume the acknowledgement messages from validation
     * @param consumerRecord
     * @throws JsonProcessingException
     */
    @KafkaListener(topics = ZeusTopics.BILLING_UPDATE_ACK)
    public void listenForAcks(
            ConsumerRecord<String, ZeusMessagePayload<Acknowledgement>> consumerRecord)
            throws JsonProcessingException{
        log.info("ACK received from premium billing for the billing update request");
        String valueAsString = objectMapper.writeValueAsString(consumerRecord.value());
        ZeusMessagePayload<Acknowledgement> ackZeusMessagePayload =
                objectMapper.readValue(valueAsString,
                        new TypeReference<ZeusMessagePayload<Acknowledgement>>(){});
        createPayloadTrackerAckDetail(ackZeusMessagePayload);
        log.info("Request payload id:{}", ackZeusMessagePayload.getPayload().getRequestPayloadId());
        log.info("Ack id:{}",ackZeusMessagePayload.getPayload().getAckId());
    }

    /**
     * Kafka listener to consume the premium billing update responses
     * @param consumerRecord
     * @throws JsonProcessingException
     */
    @KafkaListener(topics = ZeusTopics.BILLING_UPDATE_RESP)
    public void listenForBillingUpdateResponse(
            ConsumerRecord<String, ZeusMessagePayload<BillingUpdateResponse>> consumerRecord)
            throws JsonProcessingException {
        log.info("Billing Update response received:{}", consumerRecord.value().getPayload());
        String valueAsString = objectMapper.writeValueAsString(consumerRecord.value());
        log.info("Value received as response:{}", valueAsString);
        ZeusMessagePayload<BillingUpdateResponse> billingUpdateResponse =
                objectMapper.readValue(valueAsString,
                        new TypeReference<ZeusMessagePayload<BillingUpdateResponse>>() {});
        createPayloadTrackerRespDetail(billingUpdateResponse);
        transactionProcessor.postPBUpdate(billingUpdateResponse.getPayload());
    }

    /**
     * Log the details of the acknowledgment payload that was received
     * @param payload
     */
    private void createPayloadTrackerAckDetail(
            ZeusMessagePayload<Acknowledgement> payload) throws JsonProcessingException {
        String payloadAsString = objectMapper.writeValueAsString(payload);
        PayloadTrackerDetail payloadTrackerDetail = PayloadTrackerDetail.builder()
                .payloadTracker(payloadTrackerHelper.getPayloadTracker(payload.getPayload().getRequestPayloadId()))
                .responsePayload(payloadAsString)
                .responseTypeCode("ACKNOWLEDGEMENT")
                .responsePayloadId(payload.getPayload().getAckId())
                .payloadDirectionTypeCode("INBOUND")
                .sourceDestinations(payload.getMessageMetadata().getMessageSource())
                .build();
        payloadTrackerDetailHelper.createPayloadTrackerDetail(payloadTrackerDetail);
    }

    /**
     * Log the details of the response payload that was received
     * @param payload
     */
    private void createPayloadTrackerRespDetail(
            ZeusMessagePayload<BillingUpdateResponse> payload) throws JsonProcessingException {
        log.info("Payload tracker detail to be created for validation response");
        log.info("Processing Validation Response:{}", payload.getPayload());
        BillingUpdateResponse billingUpdateResponse = payload.getPayload();
        log.info("Billing Update Response:{}", billingUpdateResponse);
        String payloadAsString = objectMapper.writeValueAsString(payload);
        log.info("Payload string:{}", payloadAsString);
        PayloadTrackerDetail payloadTrackerDetail = PayloadTrackerDetail.builder()
                .payloadTracker(payloadTrackerHelper.getPayloadTracker(billingUpdateResponse.getRequestPayloadId()))
                .responsePayload(payloadAsString)
                .responseTypeCode("BILLING-UPDATE-RESPONSE")
                .responsePayloadId(billingUpdateResponse.getResponseId())
                .payloadDirectionTypeCode("INBOUND")
                .sourceDestinations(payload.getMessageMetadata().getMessageSource())
                .build();
        payloadTrackerDetailHelper.createPayloadTrackerDetail(payloadTrackerDetail);
        log.info("Payload tracker detail created for response");
    }
}
