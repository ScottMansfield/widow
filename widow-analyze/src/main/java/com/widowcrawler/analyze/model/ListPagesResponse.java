package com.widowcrawler.analyze.model;

import java.util.Set;

/**
 * @author Scott Mansfield
 */
public class ListPagesResponse {

    private boolean success;
    private String message;
    private Double capacityConsumed;
    private String startKey;
    private Set<String> pages;

    public ListPagesResponse(
            boolean success,
            String message,
            Double capacityConsumed,
            String startKey,
            Set<String> pages) {
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

    public Set<String> getPages() {
        return pages;
    }
}
