/**
 * Copyright 2015 Scott Mansfield
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.widowcrawler.core.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Scott Mansfield
 */
public class DomainUtils {

    private static final Logger logger = LoggerFactory.getLogger(DomainUtils.class);

    public static boolean isBaseDomain(String baseDomain, String url) {
        try {
            baseDomain = StringUtils.lowerCase(StringUtils.trim(baseDomain));
            String domain = StringUtils.lowerCase(StringUtils.trimToNull(new URI(url).getHost()));

            if (domain == null) {
                logger.info("Returning false because url's domain is null");
                return false;
            }

            while (StringUtils.isNotBlank(domain)) {
                if (StringUtils.equals(baseDomain, domain)) {
                    return true;
                }

                domain = StringUtils.substringAfter(domain, ".");
            }

            return false;

        } catch (URISyntaxException ex) {
            logger.error("Could not determine base domain relationship. Returning default of false.", ex);
            return false;
        }
    }
}
