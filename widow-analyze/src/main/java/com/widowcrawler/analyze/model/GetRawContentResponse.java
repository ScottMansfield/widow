package com.widowcrawler.analyze.model;

/**
 * @author Scott Mansfield
 */
public class GetRawContentResponse {

    private boolean success;
    private String message;
    private String rawContent;

    public GetRawContentResponse(
            boolean success,
            String message,
            String rawContent) {
        this.success = success;
        this.message = message;
        this.rawContent = rawContent;
    }

    public boolean getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getRawContent() {
        return rawContent;
    }
}
