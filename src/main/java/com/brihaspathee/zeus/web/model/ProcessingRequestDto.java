package com.brihaspathee.zeus.web.model;

import lombok.*;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 07, April 2024
 * Time: 7:14 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.web.model
 * To change this template use File | Settings | File and Code Template
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingRequestDto {

    /**
     * The type code of the request. This is like TRANSACTION, WEB
     */
    private String zrcnTypeCode;

    /**
     * The control number generated for the request. For transaction this will be ZTCN
     */
    private String zrcn;

    /**
     * The source from which the request was received;
     */
    private String source;

    /**
     * If the request was received asynchronously, the payload id of the request
     */
    private String requestPayloadId;

    /**
     * toString method
     * @return
     */
    @Override
    public String toString() {
        return "ProcessingRequestDto{" +
                "zrcnTypeCode='" + zrcnTypeCode + '\'' +
                ", zrcn='" + zrcn + '\'' +
                ", source='" + source + '\'' +
                ", requestPayloadId='" + requestPayloadId + '\'' +
                '}';
    }
}
