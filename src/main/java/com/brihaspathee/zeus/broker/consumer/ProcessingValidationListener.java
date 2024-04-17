package com.brihaspathee.zeus.broker.consumer;

import com.brihaspathee.zeus.broker.message.AccountProcessingResponse;
import com.brihaspathee.zeus.constants.ProcessFlowType;
import com.brihaspathee.zeus.domain.entity.PayloadTrackerDetail;
import com.brihaspathee.zeus.helper.interfaces.AddTransactionHelper;
import com.brihaspathee.zeus.helper.interfaces.PayloadTrackerDetailHelper;
import com.brihaspathee.zeus.helper.interfaces.PayloadTrackerHelper;
import com.brihaspathee.zeus.message.Acknowledgement;
import com.brihaspathee.zeus.message.ZeusMessagePayload;
import com.brihaspathee.zeus.service.interfaces.TransactionProcessor;
import com.brihaspathee.zeus.validator.TransactionValidationResult;
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
 * Date: 03, April 2024
 * Time: 12:22 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.broker.consumer
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessingValidationListener {

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
     * Add transaction processor instance to continue processing the ADD transaction
     */
    private final TransactionProcessor transactionProcessor;

    /**
     * kafka consumer to consume the acknowledgement messages from validation
     * @param consumerRecord
     * @throws JsonProcessingException
     */
    @KafkaListener(topics = "ZEUS.VALIDATOR.PROCESSING.ACK")
    public void listenForAcks(
            ConsumerRecord<String, ZeusMessagePayload<Acknowledgement>> consumerRecord
    ) throws JsonProcessingException {
        log.info("ACK received from validation service for the processing validation request");
        String valueAsString = objectMapper.writeValueAsString(consumerRecord.value());
        ZeusMessagePayload<Acknowledgement> ackZeusMessagePayload =
                objectMapper.readValue(valueAsString,
                        new TypeReference<ZeusMessagePayload<Acknowledgement>>(){});
        createPayloadTrackerAckDetail(ackZeusMessagePayload);
        log.info("Request payload id:{}", ackZeusMessagePayload.getPayload().getRequestPayloadId());
        log.info("Ack id:{}",ackZeusMessagePayload.getPayload().getAckId());
    }


    /**
     * Kafka listener to consume the validation service responses
     * @param consumerRecord
     * @throws JsonProcessingException
     */
    @KafkaListener(topics = "ZEUS.VALIDATOR.PROCESSING.RESP")
    public void listenForAccountProcessingResponse(
            ConsumerRecord<String, ZeusMessagePayload<ProcessingValidationResult>> consumerRecord
    ) throws JsonProcessingException {
        log.info("Processing Validation Response received:{}", consumerRecord.value().getPayload());
        String valueAsString = objectMapper.writeValueAsString(consumerRecord.value());
        log.info("Value received as response:{}", valueAsString);
        ZeusMessagePayload<ProcessingValidationResult> processingValidationResultPayload =
                objectMapper.readValue(valueAsString,
                        new TypeReference<ZeusMessagePayload<ProcessingValidationResult>>() {});
        createPayloadTrackerRespDetail(processingValidationResultPayload);
        log.info("About to continue processing the transaction");
        ProcessingValidationResult processingValidationResult = processingValidationResultPayload.getPayload();
        transactionProcessor.postValidationProcessing(processingValidationResult);
//        ProcessFlowType processFlowType = processingValidationResult.getValidationRequest().getProcessFlowType();
//        log.info("Process flow type is: {}", processFlowType);
//        if(processFlowType.equals(ProcessFlowType.NEW_ACCOUNT) ||
//                processFlowType.equals(ProcessFlowType.PLAN_CHANGE)){
//            addTransactionHelper.postValidationProcessing(processingValidationResult);
//        }
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
            ZeusMessagePayload<ProcessingValidationResult> payload) throws JsonProcessingException {
        log.info("Payload tracker detail to be created for validation response");
        log.info("Processing Validation Response:{}", payload.getPayload());
        ProcessingValidationResult processingValidationResult = payload.getPayload();
        log.info("Processing Validation Result:{}", processingValidationResult.isValidationPassed());
        String payloadAsString = objectMapper.writeValueAsString(payload);
        log.info("Payload string:{}", payloadAsString);
        PayloadTrackerDetail payloadTrackerDetail = PayloadTrackerDetail.builder()
                .payloadTracker(payloadTrackerHelper.getPayloadTracker(processingValidationResult.getRequestPayloadId()))
                .responsePayload(payloadAsString)
                .responseTypeCode("RESULT")
                .responsePayloadId(processingValidationResult.getResponseId())
                .payloadDirectionTypeCode("INBOUND")
                .sourceDestinations(payload.getMessageMetadata().getMessageSource())
                .build();
        payloadTrackerDetailHelper.createPayloadTrackerDetail(payloadTrackerDetail);
        log.info("Payload tracker detail created for response");
    }
}
