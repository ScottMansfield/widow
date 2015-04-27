package com.widowcrawler.analyze.resources;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.netflix.archaius.Config;
import com.widowcrawler.analyze.model.GetPageSummaryResponse;
import com.widowcrawler.core.model.PageAttribute;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Scott Mansfield
 */
@Path("summary")
public class SummaryResources {

    private static final String DYNAMO_TABLE_NAME_CONFIG_KEY = "com.widowcrawler.table.name";

    @Inject
    Config config;

    @Inject
    AmazonDynamoDB dynamoDB;

    @GET
    @Path("ping")
    public String ping() {
        return "pong";
    }

    @GET
    @Path("page/{base64Page}")
    public GetPageSummaryResponse summarizePage(
            @PathParam("base64Page") String base64Page) {

        // TODO: Figure out how to handle unicode characters here
        String decoded = new String(Base64.getUrlDecoder().decode(base64Page));

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
                .withSelect(Select.ALL_ATTRIBUTES)
                .withKeyConditions(conditionMap);

        QueryResult queryResult = dynamoDB.query(queryRequest);

        queryResult.getConsumedCapacity().getCapacityUnits();



        return new GetPageSummaryResponse(true, "testing", "tableName", -1D);
    }
}
