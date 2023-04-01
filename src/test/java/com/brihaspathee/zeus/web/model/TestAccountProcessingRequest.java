package com.brihaspathee.zeus.web.model;

import com.brihaspathee.zeus.broker.message.AccountProcessingRequest;
import com.brihaspathee.zeus.dto.account.AccountDto;
import lombok.*;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 21, March 2023
 * Time: 2:47 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.web.model
 * To change this template use File | Settings | File and Code Template
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestAccountProcessingRequest {

    /**
     * Identifies if an exception is expected
     */
    private boolean exceptionExpected;

    /**
     * The exception code when an exception is expected
     */
    private String exceptionCode;

    /**
     * The exception message when an exception is expected
     */
    private String exceptionMessage;

    /**
     * The http status code expected
     */
    private String httpStatusCode;

    /**
     * The request that sent to process the transaction
     */
    private AccountProcessingRequest accountProcessingRequest;

    /**
     * The account dto that is expected while processing the transaction
     */
    private AccountDto expectedAccountDto;

    /**
     * toString method
     * @return
     */
    @Override
    public String toString() {
        return "TestAccountProcessingRequest{" +
                "exceptionExpected=" + exceptionExpected +
                ", exceptionCode='" + exceptionCode + '\'' +
                ", exceptionMessage='" + exceptionMessage + '\'' +
                ", httpStatusCode='" + httpStatusCode + '\'' +
                ", accountProcessingRequest=" + accountProcessingRequest +
                ", expectedAccountDto=" + expectedAccountDto +
                '}';
    }
}
