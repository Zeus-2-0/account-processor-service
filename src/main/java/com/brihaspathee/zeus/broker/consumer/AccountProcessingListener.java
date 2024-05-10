package com.brihaspathee.zeus.broker.consumer;

import com.brihaspathee.zeus.broker.producer.AccountProcessingResponseProducer;
import com.brihaspathee.zeus.constants.ZeusServiceNames;
import com.brihaspathee.zeus.domain.entity.PayloadTracker;
import com.brihaspathee.zeus.domain.entity.PayloadTrackerDetail;
import com.brihaspathee.zeus.helper.interfaces.PayloadTrackerDetailHelper;
import com.brihaspathee.zeus.helper.interfaces.PayloadTrackerHelper;
import com.brihaspathee.zeus.message.Acknowledgement;
import com.brihaspathee.zeus.message.MessageMetadata;
import com.brihaspathee.zeus.message.ZeusMessagePayload;
import com.brihaspathee.zeus.service.interfaces.TransactionProcessor;
import com.brihaspathee.zeus.util.ZeusRandomStringGenerator;
import com.brihaspathee.zeus.broker.message.AccountProcessingRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 02, December 2022
 * Time: 9:33 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.broker.consumer
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountProcessingListener {

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
     * Transaction processor instance to process the transaction request
     */
    private final TransactionProcessor transactionProcessor;

    /**
     * Processing response producer instance to send the response back to TMS
     */
    private final AccountProcessingResponseProducer accountProcessingResponseProducer;

    /**
     * Kafka listener to consume the requests received from transaction manager
     * @param consumerRecord
     * @throws JsonProcessingException
     */
    @KafkaListener(topics = "ZEUS.ACCOUNT.PROCESSING.REQ")
    @SendTo(value = "ZEUS.ACCOUNT.PROCESSING.ACK")
    public ZeusMessagePayload<Acknowledgement> listenForTransactionRequest(
            ConsumerRecord<String, ZeusMessagePayload<AccountProcessingRequest>> consumerRecord
    ) throws JsonProcessingException {
        log.info("Transaction request received ");
        String valueAsString = objectMapper.writeValueAsString(consumerRecord.value());
        ZeusMessagePayload<AccountProcessingRequest> accountRequestPayload =
                objectMapper.readValue(valueAsString,
                        new TypeReference<ZeusMessagePayload<AccountProcessingRequest>>() {});

        log.info("Request received for account {}", accountRequestPayload.getPayload().getAccountNumber());
        log.info("Transaction Details {}", accountRequestPayload.getPayload().getTransactionDto());
        PayloadTracker payloadTracker = createPayloadTracker(accountRequestPayload);
        log.info("Account information received in the member management service for processing:{}", accountRequestPayload.getPayload().getAccountNumber());
        ZeusMessagePayload<Acknowledgement> ack = createAcknowledgment(payloadTracker);
        processAccount(accountRequestPayload, payloadTracker);
        log.info("Sending the ack for account {}", accountRequestPayload.getPayload().getAccountNumber());
        log.info("Ack id is {}", ack.getPayload().getAckId());
        return ack;

    }

    /**
     * Invokes the helper to create the payload tracker
     * @param payload
     */
    private PayloadTracker createPayloadTracker(ZeusMessagePayload<AccountProcessingRequest> payload) throws JsonProcessingException {
        String payloadAsString = objectMapper.writeValueAsString(payload);
        PayloadTracker payloadTracker = PayloadTracker.builder()
                .payload_key(payload.getPayload().getTransactionDto().getZtcn())
                .payload_key_type_code("TRANSACTION")
                .payloadId(payload.getPayloadId())
                .payloadDirectionTypeCode("INBOUND")
                .sourceDestinations(StringUtils.join(payload.getMessageMetadata().getMessageSource()))
                .payload(payloadAsString)
                .build();
        return payloadTrackerHelper.createPayloadTracker(payloadTracker);
    }

    /**
     * Create the acknowledgement to send back to member management service
     * @param payloadTracker
     * @return
     * @throws JsonProcessingException
     */
    private ZeusMessagePayload<Acknowledgement> createAcknowledgment(
            PayloadTracker payloadTracker) throws JsonProcessingException {
        String[] messageDestinations = {ZeusServiceNames.TRANSACTION_MANAGER};
        String ackId = ZeusRandomStringGenerator.randomString(15);
        ZeusMessagePayload<Acknowledgement> ack = ZeusMessagePayload.<Acknowledgement>builder()
                .messageMetadata(MessageMetadata.builder()
                        .messageDestination(messageDestinations)
                        .messageSource(ZeusServiceNames.ACCOUNT_PROCESSOR_SERVICE)
                        .messageCreationTimestamp(LocalDateTime.now())
                        .build())
                .payload(Acknowledgement.builder()
                        .ackId(ackId)
                        .requestPayloadId(payloadTracker.getPayloadId())
                        .build())
                .build();
        String ackAsString = objectMapper.writeValueAsString(ack);

        // Store the acknowledgement in the detail table
        PayloadTrackerDetail payloadTrackerDetail = PayloadTrackerDetail.builder()
                .payloadTracker(payloadTracker)
                .responseTypeCode("ACKNOWLEDGEMENT")
                .responsePayload(ackAsString)
                .responsePayloadId(ackId)
                .payloadDirectionTypeCode("OUTBOUND")
                .sourceDestinations(StringUtils.join(messageDestinations, ','))
                .build();
        payloadTrackerDetailHelper.createPayloadTrackerDetail(payloadTrackerDetail);
        return ack;
    }

    /**
     * This method performs the following functions
     * 1. Processes the account
     * @param messagePayload
     * @param payloadTracker
     */
    private void processAccount(ZeusMessagePayload<AccountProcessingRequest> messagePayload, PayloadTracker payloadTracker) throws JsonProcessingException {
        log.info(messagePayload.getPayload().getAccountNumber());
        AccountProcessingRequest accountProcessingRequest = messagePayload.getPayload();
        log.info("Inside process account method for the transaction:{}", accountProcessingRequest.getAccountNumber() );
        log.info("The payload tracker is:{}", payloadTracker.getPayloadId());

//        PayloadTracker finalPayloadTracker = payloadTracker;
        // Process the transaction
//        transactionProcessor.processTransaction(accountProcessingRequest, payloadTracker).;
        transactionProcessor.processTransaction(accountProcessingRequest, payloadTracker)
                .subscribe(accountProcessingResponse -> {
                    try {
                        log.info("Initial processing of the transaction is completed");
                        //accountProcessingResponseProducer.sendAccountProcessingResponse(accountProcessingResponse);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }
}
