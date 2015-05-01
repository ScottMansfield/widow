package com.widowcrawler.analyze.model;

import com.widowcrawler.core.model.PageAttribute;

import java.util.Map;

/**
 * @author Scott Mansfield
 */
public class PageVisitInfoResponse {

    private boolean success;
    private String message;
    private Double capacityConsumed;
    private Map<PageAttribute, Object> pageInfo;

    public PageVisitInfoResponse(
            boolean success,
            String message,
            Double capacityConsumed,
            Map<PageAttribute, Object> pageInfo) {
        this.success = success;
        this.message = message;
        this.capacityConsumed = capacityConsumed;
        this.pageInfo = pageInfo;
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

    public Map<PageAttribute, Object> getPageInfo() {
        return pageInfo;
    }
}
