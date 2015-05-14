package com.widowcrawler.analyze.model;

import com.widowcrawler.core.model.PageAttribute;

import java.util.List;
import java.util.Map;

/**
 * @author Scott Mansfield
 */
public class GetPageSummaryResponse {
    boolean success;
    String message;
    Double capacityConsumed;
    List<Map<PageAttribute, Object>> visits;

    public GetPageSummaryResponse(
            boolean success,
            String message,
            Double capacityConsumed,
            List<Map<PageAttribute, Object>> visits) {
        this.success = success;
        this.message = message;
        this.capacityConsumed = capacityConsumed;
        this.visits = visits;
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

    public List<Map<PageAttribute, Object>> getVisits() {
        return visits;
    }
}
