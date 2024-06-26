package com.brihaspathee.zeus.broker.producer;

import com.brihaspathee.zeus.constants.ZeusServiceNames;
import com.brihaspathee.zeus.domain.entity.PayloadTracker;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import com.brihaspathee.zeus.helper.interfaces.PayloadTrackerDetailHelper;
import com.brihaspathee.zeus.helper.interfaces.PayloadTrackerHelper;
import com.brihaspathee.zeus.message.MessageMetadata;
import com.brihaspathee.zeus.message.ZeusMessagePayload;
import com.brihaspathee.zeus.util.ZeusRandomStringGenerator;
import com.brihaspathee.zeus.validator.request.ProcessingValidationRequest;
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
 * Date: 03, April 2024
 * Time: 9:32 AM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.broker.producer
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountProcessingValidationProducer {

    /**
     * Kafka template to produce and send messages
     */
    private final KafkaTemplate<String, ZeusMessagePayload<ProcessingValidationRequest>> kafkaTemplate;

    /**
     * Object mapper that can covert the object into a string
     */
    private final ObjectMapper objectMapper;

    /**
     * Payload tracker helper instance to get the payload tracker for which the detail is being inserted
     */
    private final PayloadTrackerHelper payloadTrackerHelper;

    /**
     * Payload tracker detail helper instance to create the payload tracker detail record
     */
    private final PayloadTrackerDetailHelper payloadTrackerDetailHelper;

    /**
     * ListenableFutureCallback class that is used after success or failure of publishing the message
     */
    private final AccountProcessingValidationCallback accountProcessingValidationCallback;

    /**
     * Send the request to validation service to validation the transaction data
     * @param accountProcessingValidationRequest
     * @param requestPayloadId
     * @throws JsonProcessingException
     */
    public void sendAccountProcessingValidationRequest(ProcessingValidationRequest accountProcessingValidationRequest,
                                                       String requestPayloadId) throws JsonProcessingException{
        log.info("Inside the account processing validation producer.");
        String ztcn = accountProcessingValidationRequest.getTransactionDto().getZtcn();
        log.info("Transaction's ZTCN:{}", ztcn);
        // Create the result payload that is to be sent to the Validation
        String[] messageDestinations = {ZeusServiceNames.VALIDATION_SERVICE};
        ZeusMessagePayload<ProcessingValidationRequest> messagePayload = ZeusMessagePayload.<ProcessingValidationRequest>builder()
                .messageMetadata(MessageMetadata.builder()
                        .messageSource(ZeusServiceNames.ACCOUNT_PROCESSOR_SERVICE)
                        .messageDestination(messageDestinations)
                        .messageCreationTimestamp(LocalDateTime.now())
                        .build())
                .payload(accountProcessingValidationRequest)
                .payloadId(ZeusRandomStringGenerator.randomString(15))
                .build();
        // Create the payload tracker detail record for the validation request payload
        PayloadTracker payloadTracker = createPayloadTracker(messagePayload, requestPayloadId);
        accountProcessingValidationCallback.setAccountProcessingValidationRequest(accountProcessingValidationRequest);
        // Build the producer record
        ProducerRecord<String, ZeusMessagePayload<ProcessingValidationRequest>> producerRecord =
                buildProducerRecord(ZeusRandomStringGenerator.randomString(15), messagePayload);
        log.info("The payload tracker created for sending the transaction {} from APS to validation service {}",
                messagePayload.getPayload().getZrcn(),
                payloadTracker.getPayloadId());
        kafkaTemplate.send(producerRecord);//.addCallback(transactionValidationCallback);
        log.info("After sending the transaction {} to validation", ztcn);
    }

    /**
     * The method to build the producer record
     * @param payloadId
     * @param messagePayload
     */
    private ProducerRecord<String, ZeusMessagePayload<ProcessingValidationRequest>> buildProducerRecord(
            String payloadId,
            ZeusMessagePayload<ProcessingValidationRequest> messagePayload){
        RecordHeader messageHeader = new RecordHeader("APS Validation payload id",
                "APS Validation payload id".getBytes());
        return new ProducerRecord<>("ZEUS.VALIDATOR.PROCESSING.REQ",
                null,
                payloadId,
                messagePayload,
                Arrays.asList(messageHeader));
    }

    /**
     * Create the payload tracker record
     * @param messagePayload
     * @param requestPayloadId
     * @throws JsonProcessingException
     */
    private PayloadTracker createPayloadTracker(ZeusMessagePayload<ProcessingValidationRequest> messagePayload,
                                                String requestPayloadId)
            throws JsonProcessingException {
        String payloadAsString = objectMapper.writeValueAsString(messagePayload);
        PayloadTracker payloadTracker = PayloadTracker.builder()
                .payloadDirectionTypeCode("OUTBOUND")
                // This will be ztcn for a Transaction
                .payload_key(messagePayload.getPayload().getZrcn())
                // Type code will be "TRANSACTION" for a transaction
                .payload_key_type_code(messagePayload.getPayload().getZrcnTypeCode())
                .payload(payloadAsString)
                .payloadId(messagePayload.getPayloadId())
                .parentPayloadId(requestPayloadId)
                .sourceDestinations(StringUtils.join(
                        messagePayload.getMessageMetadata().getMessageDestination()))
                .build();
        return payloadTrackerHelper.createPayloadTracker(payloadTracker);
    }
}
