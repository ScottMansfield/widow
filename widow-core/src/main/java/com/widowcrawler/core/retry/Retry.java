package com.widowcrawler.core.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.function.Supplier;

/**
 * Provides a generic way to perform synchronous, randomized, exponential-backoff operations.
 *
 * @author Scott Mansfield
 */
public class Retry {

    private static final Logger logger = LoggerFactory.getLogger(Retry.class);

    private static final Random random = new Random();

    private static final Integer DEFAULT_RETRY_COUNT = 3;

    private static final Integer VARIANCE = 10000;

    public static <T> T retry(Supplier<T> tryMe) throws InterruptedException, RetryFailedException {
        return retry(tryMe, DEFAULT_RETRY_COUNT);
    }

    public static <T> T retry(Supplier<T> tryMe, int times) throws InterruptedException, RetryFailedException {
        Throwable latestError = null;

        for (int i = 1; i <= times; i++) {
            try {
                logger.info("Trying time " + i);
                return tryMe.get();
            } catch (Throwable t) {
                logger.error("Error while (re)trying ", t);
                latestError = t;
            }

            if (i < times) {
                int sleepTime = random.nextInt(VARIANCE) + (i * i * 1000);
                logger.info("Sleeping for " + sleepTime + "ms");
                Thread.sleep(sleepTime);
            }
        }

        throw new RetryFailedException("Retry could not complete the operation", latestError);
    }

}
