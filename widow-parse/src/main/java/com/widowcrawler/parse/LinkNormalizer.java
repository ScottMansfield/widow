package com.widowcrawler.parse;

import com.netflix.governator.annotations.AutoBindSingleton;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Scott Mansfield
 */
@AutoBindSingleton
public class LinkNormalizer {

    private static final Logger logger = LoggerFactory.getLogger(LinkNormalizer.class);

    private static final String JS_PREFIX = "javascript:";

    /**
     * Normalizes a link extracted from a page given the page's original URI.
     *
     * @param original The URI of the page that is being parsed
     * @param extracted The extracted link from the page
     * @return The normalized URI, or null if there was an error parsing the extracted URI.
     */
    public String normalize(String original, String extracted) {

        Validate.notBlank(original);
        Validate.notNull(extracted);

        if (StringUtils.startsWith(extracted, JS_PREFIX)) {
            return null;
        }

        URI originalUri = null;
        URI extractedUri = null;

        try {
            originalUri = new URI(original);
            extractedUri = new URI(extracted);
        } catch (URISyntaxException ex) {
            logger.warn("Extracted URI is invalid: " + extracted, ex);
            return null;
        }

        UriBuilder retval = UriBuilder.fromUri(extractedUri);
        boolean hasHost = StringUtils.isNotBlank(extractedUri.getHost());
        boolean hasScheme = StringUtils.isNotBlank(extractedUri.getScheme());

        if (!hasScheme) {
            retval.scheme(originalUri.getScheme());
        }

        if (!hasHost) {
            // moved to below, checking
            // TODO: Seeing IllegalArgumentException: Schema specific part is opaque
            // Maybe move this below the scheme instead of the other way around?
            retval.host(originalUri.getHost());
            String normalizedPath = normalizePath(originalUri.getPath(), extractedUri.getPath());
            retval.replacePath(normalizedPath);
        }

        retval.fragment("");

        return retval.toString();
    }

    private String normalizePath(String originalPath, String extractedPath) {

        originalPath = findPathDirectory(originalPath);

        if (StringUtils.startsWith(extractedPath, "../")) {

            while (StringUtils.startsWith(extractedPath, "../") &&
                    originalPath.length() > 0) {
                originalPath = removePathChunkAtEnd(originalPath);
                extractedPath = removePathChunkAtStart(extractedPath);
            }
        }

        originalPath = StringUtils.stripEnd(originalPath, "/");
        extractedPath = StringUtils.stripStart(extractedPath, "/");

        return originalPath + "/" + extractedPath;
    }

    private String findPathDirectory(String path) {
        int lastSlash = StringUtils.lastIndexOf(path, "/");

        if (lastSlash == path.length() - 1) {
            // already a directory
            return path;
        }

        return removePathChunkAtEnd(path);
    }

    private String removePathChunkAtEnd(String path) {
        path = StringUtils.stripEnd(path, "/");
        int lastSlash = StringUtils.lastIndexOf(path, "/");
        return StringUtils.substring(path, 0, lastSlash);
    }

    private String removePathChunkAtStart(String path) {
        path = StringUtils.stripStart(path, "/");
        int firstSlash = StringUtils.indexOf(path, "/");
        return StringUtils.substring(path, firstSlash + 1, path.length());
    }
}
