package com.flipkart.zjsonpatch;

/**
 * User: holograph
 * Date: 03/08/16
 */
public class InvalidJsonPatchException extends JsonPatchApplicationException {
    public InvalidJsonPatchException(String message) {
        super(message);
    }

    public InvalidJsonPatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidJsonPatchException(Throwable cause) {
        super(cause);
    }
}
