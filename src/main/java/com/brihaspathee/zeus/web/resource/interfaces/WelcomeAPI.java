package com.brihaspathee.zeus.web.resource.interfaces;

import com.brihaspathee.zeus.web.model.WelcomeDto;
import com.brihaspathee.zeus.web.response.ZeusApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 18, November 2022
 * Time: 4:19 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.web.resource.interfaces
 * To change this template use File | Settings | File and Code Template
 */
@RequestMapping("/api/v1/zeus/account-processor")
@Validated
public interface WelcomeAPI {

    /**
     * A welcome endpoint to check for connectivity
     * @return
     */
    @Operation(
            operationId = "Welcome API",
            method = "GET",
            description = "Get the welcome message",
            tags = {"welcome"}
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved the Welcome message",
                            content = {
                                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = WelcomeDto.class))
                            }
                    )
            }
    )
    @GetMapping("/welcome")
    ResponseEntity<ZeusApiResponse<WelcomeDto>> getWelcomeMessage();
}
