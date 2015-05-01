package com.widowcrawler.analyze.model;

import java.util.Set;

/**
 * @author Scott Mansfield
 */
public class GetPageSummaryResponse {
    boolean success;
    String message;
    Double capacityConsumed;
    Set<Long> timesAccessed;

    public GetPageSummaryResponse(
            boolean success,
            String message,
            Double capacityConsumed,
            Set<Long> timesAccessed) {
        this.success = success;
        this.message = message;
        this.capacityConsumed = capacityConsumed;
        this.timesAccessed = timesAccessed;
    }

    public boolean getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Double getCapacityConsumed() {
        return capacityConsumed;
    }

    public Set<Long> getTimesAccessed() {
        return timesAccessed;
    }
}
