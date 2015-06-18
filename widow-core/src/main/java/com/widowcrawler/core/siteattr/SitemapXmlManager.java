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
package com.widowcrawler.core.siteattr;

import com.widowcrawler.exo.Exo;
import com.widowcrawler.exo.model.Sitemap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * @author Scott Mansfield
 */
public class SitemapXmlManager {

    private static final Logger logger = LoggerFactory.getLogger(SitemapXmlManager.class);

    private static final ConcurrentMap<String, Sitemap> sitemapsByDomain;
    private static final ConcurrentMap<String, Future<Boolean>> fetchesInProgress;

    private static final Executor pool;

    static {
        sitemapsByDomain = new ConcurrentHashMap<>();
        fetchesInProgress = new ConcurrentHashMap<>();

        pool = Executors.newWorkStealingPool();
    }

    public static Sitemap getByDomain(String domain) {

        try {
            FutureTask<Boolean> fetchFuture;

            // Double-check and fetch if needed
            if (!fetchesInProgress.containsKey(domain)) {
                synchronized (SitemapXmlManager.class) {
                    if (!fetchesInProgress.containsKey(domain)) {
                        fetchFuture = new FutureTask<>(() -> {
                            Sitemap sitemap = Exo.parse(makeSitemapURL(domain));
                            sitemapsByDomain.put(domain, sitemap);
                            return Boolean.TRUE;
                        });

                        fetchesInProgress.put(domain, fetchFuture);
                        pool.execute(fetchFuture);
                    }
                }
            }

            // Past here, we should be guaranteed that the sitemapsByDomain map
            // has at least something for the given domain, even if it's empty
            fetchesInProgress.get(domain).get();

            return sitemapsByDomain.get(domain);

        } catch (InterruptedException ex) {
            logger.warn("Interrupted while trying to get Sitemap for " + domain, ex);
            Thread.currentThread().interrupt();
            return null; // useless, but necessary to compile
        } catch (ExecutionException ex) {
            logger.error("ExecutionException while trying to get Sitemap fetch in progress", ex);
            return null;
        }
    }

    private static String makeSitemapURL(String domain) {
        return String.format("http://%s/sitemap.xml", domain);
    }
}
