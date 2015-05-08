package com.widowcrawler.analyze.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Scott Mansfield
 */
public class ListPagesResponse {

    private boolean success;
    private String message;
    private Double capacityConsumed;
    private String startKey;
    private Map<String, List<Long>> pages;

    public ListPagesResponse(
            boolean success,
            String message,
            Double capacityConsumed,
            String startKey,
            Map<String, List<Long>> pages) {
        this.success = success;
        this.message = message;
        this.capacityConsumed = capacityConsumed;
        this.startKey = startKey;
        this.pages = pages;
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

    public String getStartKey() {
        return startKey;
    }

    public Map<String, List<Long>> getPages() {
        return pages;
    }
}
