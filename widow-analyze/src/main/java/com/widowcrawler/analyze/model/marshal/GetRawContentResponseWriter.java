package com.widowcrawler.analyze.model.marshal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.widowcrawler.analyze.model.GetRawContentResponse;

import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author Scott Mansfield
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class GetRawContentResponseWriter implements MessageBodyWriter<GetRawContentResponse> {

    @Inject
    ObjectMapper objectMapper;

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == GetRawContentResponse.class;
    }

    @Override
    public long getSize(GetRawContentResponse getRawContentResponse, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(GetRawContentResponse getRawContentResponse, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        objectMapper.writeValue(entityStream, getRawContentResponse);
    }
}
