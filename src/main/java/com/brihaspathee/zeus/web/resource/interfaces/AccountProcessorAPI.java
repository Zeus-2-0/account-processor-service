package com.brihaspathee.zeus.web.resource.interfaces;

import com.brihaspathee.zeus.exception.ApiExceptionList;
import com.brihaspathee.zeus.broker.message.AccountProcessingRequest;
import com.brihaspathee.zeus.broker.message.AccountProcessingResponse;
import com.brihaspathee.zeus.web.response.ZeusApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 23, November 2022
 * Time: 3:56 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.web.resource.interfaces
 * To change this template use File | Settings | File and Code Template
 */
@RequestMapping("/api/v1/zeus/account-processor/process")
@Validated
public interface AccountProcessorAPI {

    /**
     * Process the transaction
     * @param accountProcessingRequest
     * @return
     */
    @Operation(
            operationId = "Process the transaction for the account",
            method = "POST",
            description = "Process the transaction for the account",
            tags = {"account"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Transaction successfully processed for the account",
                    content = {
                            @Content(mediaType = "application/json",schema = @Schema(implementation = AccountProcessingResponse.class))
                    }),
            @ApiResponse(responseCode = "400",
                    description = "Bad Request",
                    content = {
                            @Content(mediaType = "application/json",schema = @Schema(implementation = ApiExceptionList.class))
                    }),
            @ApiResponse(responseCode = "409",
                    description = "Conflict",
                    content = {
                            @Content(mediaType = "application/json",schema = @Schema(implementation = ApiExceptionList.class))
                    })
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ZeusApiResponse<AccountProcessingResponse>> processTransaction(
            @RequestBody @Valid AccountProcessingRequest accountProcessingRequest) throws JsonProcessingException;
}
