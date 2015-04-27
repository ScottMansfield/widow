package com.widowcrawler.analyze.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * @author Scott Mansfield
 */
public class GetPageSummaryResponse {
    boolean success;
    String message;
    String tableName;
    Double capacityConsumed;
    //Map<String, String>

    @JsonCreator
    public GetPageSummaryResponse(
            @JsonProperty("success")          boolean success,
            @JsonProperty("message")          String message,
            @JsonProperty("tableName")        String tableName,
            @JsonProperty("capacityConsumed") Double capacityConsumed) {
        this.success = success;
        this.message = message;
        this.tableName = tableName;
        this.capacityConsumed = capacityConsumed;
    }

    public boolean getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getTableName() {
        return tableName;
    }

    public Double getCapacityConsumed() {
        return capacityConsumed;
    }
}
