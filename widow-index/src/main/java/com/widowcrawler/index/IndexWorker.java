package com.widowcrawler.index;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity;
import com.widowcrawler.core.model.IndexInput;
import com.widowcrawler.core.model.PageAttribute;
import com.widowcrawler.core.worker.Worker;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;
import java.util.stream.Collectors;

import static com.widowcrawler.core.retry.Retry.retry;

/**
 * @author Scott Mansfield
 */
public class IndexWorker extends Worker {

    private static final Logger logger = LoggerFactory.getLogger(IndexWorker.class);

    // TODO: I really ought to get working on a config system for this...
    private static final String TABLE_NAME = "widow-test";

    @Inject
    AmazonDynamoDB dynamoDBClient;

    private IndexInput indexInput;

    public IndexWorker withInput(IndexInput indexInput) {
        this.indexInput = indexInput;
        return this;
    }

    @Override
    protected boolean doWork() {
        try {
            logger.info("Received IndexInput: " + indexInput.getAttribute(PageAttribute.ORIGINAL_URL));

            // Check key fields to make sure they exist
            if (!indexInput.getAttributes().keySet().contains(PageAttribute.ORIGINAL_URL) ||
                    !indexInput.getAttributes().keySet().contains(PageAttribute.TIME_ACCESSED)) {
                throw new IllegalArgumentException("Attributes ORIGINAL_URL and TIME_ACCESSED must exist");
            }

            Map<String, AttributeValue> attributeValueMap = indexInput.getAttributes().entrySet()
                    .stream()
                    .filter((entry) -> entry.getValue() != null)
                    .filter((entry) -> StringUtils.isNotBlank(entry.getValue().toString()))
                    .collect(Collectors.toMap(
                            e -> e.getKey().toString(),
                            e -> {
                                switch (e.getKey().getType()) {
                                    case NUMBER:
                                        return new AttributeValue().withN(e.getValue().toString());

                                    case STRING:
                                    default:
                                        return new AttributeValue().withS(e.getValue().toString());
                                }
                            }
                    ));

            PutItemRequest putItemRequest = new PutItemRequest()
                    .withTableName(TABLE_NAME)
                    .withItem(attributeValueMap)
                    .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);

            final PutItemResult putItemResult = retry(() -> dynamoDBClient.putItem(putItemRequest));

            logger.info("Consumed table capacity: " + putItemResult.getConsumedCapacity().getCapacityUnits());

            return true;

        } catch (Exception ex) {
            logger.error("Error while indexing.", ex);
            return false;
        }
    }
}
