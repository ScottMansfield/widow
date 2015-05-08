package com.widowcrawler.core.model;

/**
 * @author Scott Mansfield
 */
public enum PageAttribute {
    ORIGINAL_URL(Type.STRING),
    REFERRER(Type.STRING),
    STATUS_CODE(Type.NUMBER),
    HEADERS(Type.HASH),
    PAGE_CONTENT_REF(Type.STRING),
    LOCALE(Type.STRING),
    TIME_ACCESSED(Type.NUMBER),
    LOAD_TIME_MILLIS(Type.NUMBER),
    RESPONSE_SIZE(Type.NUMBER),
    TITLE(Type.STRING),
    OUT_LINKS(Type.ARRAY),
    CSS_LINKS(Type.ARRAY),
    JS_LINKS(Type.ARRAY),
    IMG_LINKS(Type.ARRAY),
    CONTENT_SIZE(Type.NUMBER),
    SIZE_WITH_ASSETS(Type.NUMBER);

    public static enum Type {
        STRING,
        NUMBER,
        HASH,
        ARRAY
    }

    private Type type;

    private PageAttribute(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
