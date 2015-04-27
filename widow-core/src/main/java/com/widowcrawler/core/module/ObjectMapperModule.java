package com.widowcrawler.core.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.inject.AbstractModule;

/**
 * @author Scott Mansfield
 */
public class ObjectMapperModule extends AbstractModule {
    @Override
    protected void configure() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        bind(ObjectMapper.class).toInstance(mapper);
    }
}
