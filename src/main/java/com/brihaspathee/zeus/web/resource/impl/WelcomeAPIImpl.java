package com.brihaspathee.zeus.web.resource.impl;

import com.brihaspathee.zeus.web.model.WelcomeDto;
import com.brihaspathee.zeus.web.resource.interfaces.WelcomeAPI;
import com.brihaspathee.zeus.web.response.ZeusApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 18, November 2022
 * Time: 4:20 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.web.resource.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class WelcomeAPIImpl implements WelcomeAPI {

    /**
     * Returns a simple welcome message for the service
     * @return
     */
    @Override
    public ResponseEntity<ZeusApiResponse<WelcomeDto>> getWelcomeMessage() {
        return ResponseEntity.ok(ZeusApiResponse.<WelcomeDto>builder()
                .developerMessage("Connection is good")
                .message("Hello, Welcome to Zeus Account Processor Service")
                .statusCode(200)
                .status(HttpStatus.OK)
                //.timestamp(LocalDateTime.now())
                .response(WelcomeDto.builder()
                        .welcomeMessage("Hello, I am glad that you are here!!! I am sure you will have a great time")
                        .build())
                .build());
    }
}
