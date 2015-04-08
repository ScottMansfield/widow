package com.widowcrawler.fetch;

import com.widowcrawler.core.runner.AppRunner;
import com.widowcrawler.fetch.module.WidowFetchModule;

/**
 * @author Scott Mansfield
 */
public class Main {

    public static void main(String[] args) throws Exception {
        AppRunner.run(WidowFetchModule.class);
    }
}
