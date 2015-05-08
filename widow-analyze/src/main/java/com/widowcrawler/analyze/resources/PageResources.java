package com.widowcrawler.analyze.resources;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.archaius.Config;
import com.widowcrawler.analyze.model.GetPageSummaryResponse;
import com.widowcrawler.analyze.model.ListPagesResponse;
import com.widowcrawler.analyze.model.PageVisitInfoResponse;
import com.widowcrawler.core.model.PageAttribute;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Scott Mansfield
 */
@Path("pages")
public class PageResources {

    private static final Logger logger = LoggerFactory.getLogger(PageResources.class);

    private static final String DYNAMO_TABLE_NAME_CONFIG_KEY = "com.widowcrawler.table.name";

    @Inject
    Config config;

    @Inject
    AmazonDynamoDB dynamoDB;

    @Inject
    ObjectMapper objectMapper;

    @GET
    public Response getAllPages(@QueryParam("last") String startKey) {
        try {

            String tableName = config.getString(DYNAMO_TABLE_NAME_CONFIG_KEY);

            ScanRequest scanRequest = new ScanRequest()
                    .withTableName(tableName)
                    .withAttributesToGet(
                            PageAttribute.ORIGINAL_URL.toString(),
                            PageAttribute.TIME_ACCESSED.toString()
                    );

            if (StringUtils.isNotBlank(startKey)) {
                byte[] decodedLEK = Base64.getUrlDecoder().decode(startKey);

                Map<String, AttributeValue> exclusiveStartKey = objectMapper.readValue(decodedLEK, Map.class);

                scanRequest.withExclusiveStartKey(exclusiveStartKey);
            }

            final ScanResult scanResult = dynamoDB.scan(scanRequest);

            Map<String, List<Long>> pagesAndTimes = new HashMap<>();

            // For each URL, extract the different times accessed
            scanResult.getItems().forEach(
                    row -> {
                        String pageURL = row.get(PageAttribute.ORIGINAL_URL.toString()).getS();
                        List<Long> timesList = null;

                        if (pagesAndTimes.containsKey(pageURL)) {
                            timesList = pagesAndTimes.get(pageURL);
                        } else {
                            timesList = new ArrayList<>();
                        }

                        timesList.add(Long.valueOf(row.get(PageAttribute.TIME_ACCESSED.toString()).getN()));
                        pagesAndTimes.put(pageURL, timesList);
                    }
            );

            Double consumedCapacity = null;

            if (scanResult.getConsumedCapacity() != null) {
                consumedCapacity = scanResult.getConsumedCapacity().getCapacityUnits();
            }

            // TODO: Set the startKey appropriately

            return Response.ok(new ListPagesResponse(
                    true,
                    "Page listing successful",
                    consumedCapacity,
                    null,
                    pagesAndTimes
            )).build();

        } catch (Exception ex) {
            String message = "Getting pages failed. Error: " + ex.getClass().getName() + ": " + ex.getMessage();
            logger.error(message, ex);
            return Response.serverError().entity(new ListPagesResponse(false, message, null, null, null)).build();
        }
    }

    @GET
    @Path("{base64Page}")
    public Response summarizePage(@PathParam("base64Page") String base64Page) {

        try {
            String decoded = URLDecoder.decode(new String(Base64.getUrlDecoder().decode(base64Page)), "utf-8");

            String tableName = config.getString(DYNAMO_TABLE_NAME_CONFIG_KEY);

            Map<String, Condition> conditionMap = new HashMap<String, Condition>() {{
                put(PageAttribute.ORIGINAL_URL.toString(),
                        new Condition()
                                .withComparisonOperator(ComparisonOperator.EQ)
                                .withAttributeValueList(new AttributeValue().withS(decoded)));
            }};

            QueryRequest queryRequest = new QueryRequest()
                    .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
                    .withTableName(tableName)
                    .withSelect(Select.SPECIFIC_ATTRIBUTES)
                    .withAttributesToGet(PageAttribute.TIME_ACCESSED.toString())
                    .withKeyConditions(conditionMap);

            QueryResult queryResult = dynamoDB.query(queryRequest);

            Double capacityConsumed = null;

            if (queryResult.getConsumedCapacity() != null) {
                capacityConsumed = queryResult.getConsumedCapacity().getCapacityUnits();
            }

            final Set<Long> timesAccessed = queryResult.getItems().stream()
                    .map(row -> Long.valueOf(row.get(PageAttribute.TIME_ACCESSED.toString()).getN()))
                    .collect(Collectors.toSet());

            return Response.ok(new GetPageSummaryResponse(
                    true,
                    "Page summary successful",
                    capacityConsumed,
                    timesAccessed
            )).build();

        } catch (Exception ex) {
            String message = "Getting page summary failed. Error: " + ex.getMessage();
            logger.error(message, ex);
            return Response.serverError().entity(new GetPageSummaryResponse(false, message, null, null)).build();
        }
    }

    @GET
    @Path("{base64Page}/{timeAccessed}")
    public Response getPageVisitInfo(
            @PathParam("base64Page")   String base64Page,
            @PathParam("timeAccessed") Long timeAccessed) {

        try {
            // TODO: Figure out how to handle unicode characters here
            String decoded = URLDecoder.decode(new String(Base64.getUrlDecoder().decode(base64Page)), "utf-8");

            String tableName = config.getString(DYNAMO_TABLE_NAME_CONFIG_KEY);

            Map<String, Condition> conditionMap = new HashMap<String, Condition>() {{
                put(PageAttribute.ORIGINAL_URL.toString(),
                        new Condition()
                                .withComparisonOperator(ComparisonOperator.EQ)
                                .withAttributeValueList(new AttributeValue().withS(decoded)));
                put(PageAttribute.TIME_ACCESSED.toString(),
                        new Condition()
                                .withComparisonOperator(ComparisonOperator.EQ)
                                .withAttributeValueList(new AttributeValue().withN(timeAccessed.toString())));
            }};

            QueryRequest queryRequest = new QueryRequest()
                    .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
                    .withTableName(tableName)
                    .withSelect(Select.ALL_ATTRIBUTES)
                    .withKeyConditions(conditionMap);

            QueryResult queryResult = dynamoDB.query(queryRequest);

            Double capacityConsumed = null;

            if (queryResult.getConsumedCapacity() != null) {
                capacityConsumed = queryResult.getConsumedCapacity().getCapacityUnits();
            }

            final Map<PageAttribute, Object> attributeValueMap = queryResult.getItems().get(0)
                    .entrySet().stream()
                    .collect(Collectors.<Map.Entry<String, AttributeValue>, PageAttribute, Object>toMap(
                            data -> PageAttribute.valueOf(data.getKey()),
                            data -> {
                                try {
                                    AttributeValue av = data.getValue();
                                    PageAttribute key = PageAttribute.valueOf(data.getKey());

                                    switch (key.getType()) {
                                        case NUMBER:
                                            return Long.valueOf(av.getN());
                                        case HASH:
                                            // TODO: Seeing errors on parsing because of bad formatting
                                            return objectMapper.readValue(av.getS(), Map.class);
                                        case ARRAY:
                                            return objectMapper.readValue(av.getS(), List.class);
                                        case STRING:
                                        default:
                                            return av.getS();
                                    }
                                } catch (Exception ex) {
                                    logger.error("Error converting", ex);
                                    return "";
                                }
                            }
                    ));

            return Response.ok(new PageVisitInfoResponse(
                    true,
                    "Page info get successful",
                    capacityConsumed,
                    attributeValueMap
            )).build();

        } catch (Exception ex) {
            String message = "Getting page summary failed. Error: " + ex.getMessage();
            logger.error(message, ex);
            return Response.serverError().entity(new GetPageSummaryResponse(false, message, null, null)).build();
        }
    }
}
