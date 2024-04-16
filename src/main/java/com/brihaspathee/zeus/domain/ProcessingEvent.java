package com.brihaspathee.zeus.domain;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 11, April 2024
 * Time: 1:51 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.domain
 * To change this template use File | Settings | File and Code Template
 */
public enum ProcessingEvent {

    VALIDATE,
    VALIDATION_ERROR,
    PROCESSING,
    PROCESSING_ERROR,
    SENT_TO_MMS,
    MMS_APPROVED,
    MMS_ERROR
}
