package com.widowcrawler.core.retry;

/**
 * @author Scott Mansfield
 */
public class RetryFailedException extends Exception {

    public RetryFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
