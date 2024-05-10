package com.brihaspathee.zeus.exception;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 24, April 2024
 * Time: 1:38 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.exception
 * To change this template use File | Settings | File and Code Template
 */
public class ProcessingRequestNotFoundException extends RuntimeException{

    public ProcessingRequestNotFoundException(String message){
        super(message);
    }

    public ProcessingRequestNotFoundException(String message, Throwable cause){
        super(message, cause);
    }
}
