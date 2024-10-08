package com.brihaspathee.zeus.web.resource.impl;

import com.brihaspathee.zeus.constants.ApiResponseConstants;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.account.EnrollmentSpanStatusDto;
import com.brihaspathee.zeus.service.interfaces.AccountService;
import com.brihaspathee.zeus.service.interfaces.RequestService;
import com.brihaspathee.zeus.service.interfaces.TransactionProcessor;
import com.brihaspathee.zeus.broker.message.AccountProcessingRequest;
import com.brihaspathee.zeus.web.resource.interfaces.AccountProcessorAPI;
import com.brihaspathee.zeus.web.response.ZeusApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 23, November 2022
 * Time: 4:02 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.web.resource.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class AccountProcessorAPIImpl implements AccountProcessorAPI {

    /**
     * The transaction processor instance to process the transaction
     */
    private final TransactionProcessor transactionProcessor;

    /**
     * Account service instance
     */
    private final AccountService accountService;

    /**
     * Request Service instance
     */
    private final RequestService requestService;

    /**
     * Process the transaction
     * @param accountProcessingRequest
     * @param sendToMMS
     * @return
     * @throws JsonProcessingException
     */
    @Override
    public ResponseEntity<ZeusApiResponse<AccountDto>> processTransaction(
            AccountProcessingRequest accountProcessingRequest,
            boolean sendToMMS) throws JsonProcessingException {
//        log.info("Inside the account processor resource:{}", accountProcessingRequest.getTransactionDto());
        AccountDto accountDto = transactionProcessor.processTransaction(accountProcessingRequest, sendToMMS);
        ZeusApiResponse<AccountDto> apiResponse = ZeusApiResponse.<AccountDto>builder()
                .message(ApiResponseConstants.SUCCESS)
                .developerMessage(ApiResponseConstants.SUCCESS_REASON)
                .statusCode(200)
                .status(HttpStatus.OK)
                .response(accountDto)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get the status of the enrollment span
     * @param spanStatusDto
     * @return
     */
    @Override
    public ResponseEntity<ZeusApiResponse<String>> getEnrollmentSpanStatus(EnrollmentSpanStatusDto spanStatusDto) {
        String enrollmentSpanStatus = accountService.determineEnrollmentSpanStatus(spanStatusDto);
        ZeusApiResponse<String> apiResponse = ZeusApiResponse.<String>builder()
                .response(enrollmentSpanStatus)
                .statusCode(200)
                .status(HttpStatus.OK)
                .developerMessage(ApiResponseConstants.SUCCESS)
                .message(ApiResponseConstants.SUCCESS_REASON)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Delete data by ZRCN
     * @param zrcn
     * @return
     */
    @Override
    public ResponseEntity<ZeusApiResponse<String>> cleanUp(String zrcn) {
        requestService.deleteByZrcn(zrcn);
        ZeusApiResponse<String> apiResponse = ZeusApiResponse.<String>builder()
                .response("Request deleted successfully")
                .statusCode(204)
                .status(HttpStatus.NO_CONTENT)
                .developerMessage(ApiResponseConstants.SUCCESS)
                .message(ApiResponseConstants.SUCCESS_REASON)
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.NO_CONTENT);
    }

    /**
     * Clean up the entire DB
     * @return
     */
    @Override
    public ResponseEntity<ZeusApiResponse<String>> cleanUp() {
        requestService.deleteAll();
        ZeusApiResponse<String> apiResponse = ZeusApiResponse.<String>builder()
                .response("Request deleted successfully")
                .statusCode(204)
                .status(HttpStatus.NO_CONTENT)
                .developerMessage(ApiResponseConstants.SUCCESS)
                .message(ApiResponseConstants.SUCCESS_REASON)
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.NO_CONTENT);
    }
}
