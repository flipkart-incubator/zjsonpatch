package com.flipkart.zjsonpatch;

/**
 * Created by nikhil.kumar
 * Created on 13/05/16.
 * Custom exception to be thrown in case of failures.
 */
public class ZJsonProcessingException extends RuntimeException {
    public ZJsonProcessingException(String message) {
        super(message);
    }
}
