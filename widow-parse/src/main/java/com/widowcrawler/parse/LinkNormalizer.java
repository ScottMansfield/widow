package com.widowcrawler.parse;

import com.netflix.governator.annotations.AutoBindSingleton;
import org.apache.commons.lang3.StringUtils;
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

    /**
     * Normalizes a link extracted from a page given the page's original URI.
     *
     * @param original The URI of the page that is being parsed
     * @param extracted The extracted link from the page
     * @return The normalized URI, or null if there was an error parsing the extracted URI.
     */
    public String normalize(String original, String extracted) {
        // host && protocol only, for now
        // TODO: What about inline javascript in links?

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

        if (StringUtils.isBlank(extractedUri.getScheme())) {
            retval.scheme(originalUri.getScheme());
        }

        if (StringUtils.isBlank(extractedUri.getHost())) {
            retval.host(originalUri.getHost());
        }

        retval.fragment("");

        return retval.toString();
    }
}
