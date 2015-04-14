package com.widowcrawler.core.model;

/**
 * @author Scott Mansfield
 */
public enum PageAttribute {
    ORIGINAL_URL,
    STATUS_CODE(Type.NUMBER),
    HEADERS,
    PAGE_CONTENT_REF,
    LOCALE,
    TIME_ACCESSED(Type.NUMBER),
    LOAD_TIME_MILLIS(Type.NUMBER),
    RESPONSE_SIZE(Type.NUMBER),
    TITLE,
    OUT_LINKS,
    CSS_LINKS,
    JS_LINKS,
    IMG_LINKS,
    CONTENT_SIZE(Type.NUMBER),
    SIZE_WITH_ASSETS(Type.NUMBER);

    public static enum Type {
        STRING,
        NUMBER
    }

    private Type type;

    private PageAttribute() {
        this.type = Type.STRING;
    }

    private PageAttribute(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
