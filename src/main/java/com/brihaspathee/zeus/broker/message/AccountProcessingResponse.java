package com.brihaspathee.zeus.broker.message;

import lombok.*;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 23, November 2022
 * Time: 4:02 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.web.model
 * To change this template use File | Settings | File and Code Template
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountProcessingResponse {

    /**
     * A unique id that is created as a response for processing the account information
     */
    private String responseId;

    /**
     * The request payload id for which the response is sent
     */
    private String requestPayloadId;

    /**
     * Account number for which the response is created.
     */
    private String accountNumber;

    /**
     * Transaction for which the response is created
     */
    private String ztcn;

    /**
     * Response code that indicates if the response is for one of the below
     * 1. Validations Completed
     * 2. Processing Completed
     * 3. Account Update Completed
     */
    private String responseCode;

    /**
     * Response message corresponding to the response code
     */
    private String responseMessage;

    /**
     * toString method
     * @return
     */
    @Override
    public String toString() {
        return "AccountProcessingResponse{" +
                "responseId='" + responseId + '\'' +
                ", requestPayloadId='" + requestPayloadId + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", ztcn='" + ztcn + '\'' +
                ", responseCode='" + responseCode + '\'' +
                ", responseMessage='" + responseMessage + '\'' +
                '}';
    }
}
