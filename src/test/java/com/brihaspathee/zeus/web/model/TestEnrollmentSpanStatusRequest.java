package com.brihaspathee.zeus.web.model;

import com.brihaspathee.zeus.dto.account.EnrollmentSpanStatusDto;
import lombok.*;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 07, March 2023
 * Time: 2:18 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.web.model
 * To change this template use File | Settings | File and Code Template
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestEnrollmentSpanStatusRequest {

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
     * The enrollment span status dto that is sent in as input
     */
    private EnrollmentSpanStatusDto enrollmentSpanStatusDto;

    /**
     * The expected enrollment span status
     */
    private String expectedEnrollmentSpanStatus;

    /**
     * toString method
     * @return
     */
    @Override
    public String toString() {
        return "TestEnrollmentSpanStatusRequest{" +
                "exceptionExpected=" + exceptionExpected +
                ", exceptionCode='" + exceptionCode + '\'' +
                ", exceptionMessage='" + exceptionMessage + '\'' +
                ", httpStatusCode='" + httpStatusCode + '\'' +
                ", enrollmentSpanStatusDto=" + enrollmentSpanStatusDto +
                ", expectedEnrollmentSpanStatus=" + expectedEnrollmentSpanStatus +
                '}';
    }
}
