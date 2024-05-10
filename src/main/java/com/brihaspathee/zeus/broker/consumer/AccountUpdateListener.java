package com.brihaspathee.zeus.broker.consumer;

import com.brihaspathee.zeus.broker.message.AccountProcessingResponse;
import com.brihaspathee.zeus.broker.producer.AccountProcessingResponseProducer;
import com.brihaspathee.zeus.domain.entity.PayloadTracker;
import com.brihaspathee.zeus.domain.entity.PayloadTrackerDetail;
import com.brihaspathee.zeus.helper.interfaces.PayloadTrackerDetailHelper;
import com.brihaspathee.zeus.helper.interfaces.PayloadTrackerHelper;
import com.brihaspathee.zeus.message.Acknowledgement;
import com.brihaspathee.zeus.message.ZeusMessagePayload;
import com.brihaspathee.zeus.broker.message.AccountUpdateResponse;
import com.brihaspathee.zeus.service.interfaces.TransactionProcessor;
import com.brihaspathee.zeus.util.ZeusRandomStringGenerator;
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
 * Date: 05, December 2022
 * Time: 4:05 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.broker.consumer
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountUpdateListener {

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
     * Processing response producer instance to send the response back to TMS
     */
    private final AccountProcessingResponseProducer accountProcessingResponseProducer;

    /**
     * Transaction processor instance to continue processing the transaction
     */
    private final TransactionProcessor transactionProcessor;

    /**
     * Kafka consumer to consume the acknowledgment messages from MMS
     * @param consumerRecord
     * @throws JsonProcessingException
     */
    @KafkaListener(topics = "ZEUS.ACCOUNT.UPDATE.ACK")
    public void listenForAcks(
            ConsumerRecord<String, ZeusMessagePayload<Acknowledgement>> consumerRecord) throws JsonProcessingException {
        log.info("ACK received from MMS");
        String valueAsString = objectMapper.writeValueAsString(consumerRecord.value());
        ZeusMessagePayload<Acknowledgement> ackZeusMessagePayload =
                objectMapper.readValue(valueAsString,
                        new TypeReference<ZeusMessagePayload<Acknowledgement>>(){});
        createPayloadTrackerAckDetail(ackZeusMessagePayload);
        log.info("Request payload id:{}", ackZeusMessagePayload.getPayload().getRequestPayloadId());
        log.info("Ack id:{}",ackZeusMessagePayload.getPayload().getAckId());
    }

    /**
     * Kafka consumer to consume the responses messages from MMS
     * @param consumerRecord
     * @throws JsonProcessingException
     */
    @KafkaListener(topics = "ZEUS.ACCOUNT.UPDATE.RESP")
    public void listenForAccountUpdateResponse(
            ConsumerRecord<String, ZeusMessagePayload<AccountUpdateResponse>> consumerRecord) throws JsonProcessingException {
        log.info("Validation Response received from MMS");
        String valueAsString = objectMapper.writeValueAsString(consumerRecord.value());
        ZeusMessagePayload<AccountUpdateResponse> accountValidationResultPayload =
                objectMapper.readValue(valueAsString,
                        new TypeReference<ZeusMessagePayload<AccountUpdateResponse>>() {});
        createPayloadTrackerRespDetail(accountValidationResultPayload);
        transactionProcessor.postMMSUpdate(accountValidationResultPayload.getPayload());
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
            ZeusMessagePayload<AccountUpdateResponse> payload) throws JsonProcessingException {
        String payloadAsString = objectMapper.writeValueAsString(payload);
        PayloadTracker payloadTracker = payloadTrackerHelper.getPayloadTracker(payload.getPayload().getRequestPayloadId());
        PayloadTrackerDetail payloadTrackerDetail = PayloadTrackerDetail.builder()
                .payloadTracker(payloadTracker)
                .responsePayload(payloadAsString)
                .responseTypeCode("ACCOUNT-UPDATE-RESPONSE")
                .responsePayloadId(payload.getPayload().getResponseId())
                .payloadDirectionTypeCode("INBOUND")
                .sourceDestinations(payload.getMessageMetadata().getMessageSource())
                .build();
        payloadTrackerDetailHelper.createPayloadTrackerDetail(payloadTrackerDetail);
//        if(payloadTracker.getParentPayloadId() != null){
//            log.info("Parent Payload is not null - Hence sending the response back to TMS");
//            PayloadTracker parentPayloadTracker = payloadTrackerHelper.getPayloadTracker(
//                    payloadTracker.getParentPayloadId());
//            AccountProcessingResponse accountProcessingResponse = AccountProcessingResponse.builder()
//                    .responseId(ZeusRandomStringGenerator.randomString(15))
//                    .requestPayloadId(parentPayloadTracker.getPayloadId())
//                    .accountNumber(payloadTracker.getPayload_key())
//                    .ztcn(parentPayloadTracker.getPayload_key())
//                    .responseCode("8000003")
//                    .responseMessage("Account Update Confirmation received from MMS")
//                    .build();
//            log.info("Account Processing response to be sent after mms response:{}", accountProcessingResponse);
//            transactionProcessor.postMMSUpdate(payload.getPayload());
//            accountProcessingResponseProducer.sendAccountProcessingResponse(accountProcessingResponse);
//        }

    }
}
