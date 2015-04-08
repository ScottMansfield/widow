package com.widowcrawler.parse;

import com.widowcrawler.core.runner.AppRunner;
import com.widowcrawler.parse.module.WidowParseModule;

/**
 * @author Scott Mansfield
 */
public class Main {

    public static void main(String[] args) throws Exception {
        AppRunner.run(WidowParseModule.class);
    }
}
