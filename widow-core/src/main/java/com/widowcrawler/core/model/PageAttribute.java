package com.widowcrawler.core.model;

/**
 * @author Scott Mansfield
 */
public enum PageAttribute {
    ORIGINAL_URL(Type.STRING),
    REFERRER(Type.STRING),
    STATUS_CODE(Type.LONG),
    HEADERS(Type.HASH),
    PAGE_CONTENT_REF(Type.STRING),
    LOCALE(Type.STRING),
    TIME_ACCESSED(Type.LONG),
    LOAD_TIME_MILLIS(Type.DOUBLE),
    RESPONSE_SIZE(Type.LONG),
    TITLE(Type.STRING),
    OUT_LINKS(Type.ARRAY),
    OUT_LINKS_RAW(Type.ARRAY),
    CSS_LINKS(Type.ARRAY),
    CSS_LINKS_RAW(Type.ARRAY),
    JS_LINKS(Type.ARRAY),
    JS_LINKS_RAW(Type.ARRAY),
    IMG_LINKS(Type.ARRAY),
    IMG_LINKS_RAW(Type.ARRAY),
    CONTENT_SIZE(Type.LONG),
    SIZE_WITH_ASSETS(Type.LONG);

    public static enum Type {
        STRING,
        LONG,
        DOUBLE,
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
