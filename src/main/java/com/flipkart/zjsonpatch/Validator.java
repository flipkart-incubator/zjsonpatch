package com.flipkart.zjsonpatch;

/**
 * Created by nikhil.kumar
 * created on 13/05/16.
 * Static Class to validate expressions, should always return {@link ZJsonProcessingException}.
 */
public final class Validator {
    public static void validateExpression(boolean expression, String message) {
        if(expression) {
            throw new ZJsonProcessingException(message);
        }
    }

    public static <T> T validateNull(T reference, String message) {
        if(reference == null) {
            throw new ZJsonProcessingException(message);
        }
        return reference;
    }
}
