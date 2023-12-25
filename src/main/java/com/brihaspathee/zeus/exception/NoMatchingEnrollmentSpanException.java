package com.brihaspathee.zeus.exception;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 05, December 2023
 * Time: 3:43 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.exception
 * To change this template use File | Settings | File and Code Template
 */
public class NoMatchingEnrollmentSpanException extends RuntimeException{

    /**
     * Constructor with the message
     * @param message
     */
    public NoMatchingEnrollmentSpanException(String message){
        super(message);
    }

    /**
     * Constructor with the message and cause
     * @param message
     * @param cause
     */
    public NoMatchingEnrollmentSpanException(String message, Throwable cause){
        super(message, cause);
    }
}
