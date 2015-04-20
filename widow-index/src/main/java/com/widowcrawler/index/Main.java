package com.widowcrawler.index;

import com.widowcrawler.core.runner.AppRunner;
import com.widowcrawler.index.module.WidowIndexModule;

/**
 * @author Scott Mansfield
 */
public class Main {

    public static void main(String[] args) throws Exception {
        AppRunner.run(WidowIndexModule.class, "widow-index");
    }
}
