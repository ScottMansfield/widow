package com.widowcrawler.index;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.widowcrawler.core.model.IndexInput;
import com.widowcrawler.core.model.PageAttribute;
import com.widowcrawler.core.retry.RetryFailedException;
import com.widowcrawler.core.worker.Worker;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.stream.Collectors;

import static com.widowcrawler.core.retry.Retry.retry;

/**
 * @author Scott Mansfield
 */
public class IndexWorker extends Worker {

    private static final Logger logger = LoggerFactory.getLogger(IndexWorker.class);

    private static final String TABLE_NAME_CONFIG_KEY = "com.widowcrawler.table.name";

    @Inject
    AmazonDynamoDB dynamoDBClient;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    DataSource dataSource;

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

            writeToDynamo();
            //writeToRDBMS();

            return true;

        } catch (Exception ex) {
            logger.error("Error while indexing.", ex);
            return false;
        }
    }

    private void writeToDynamo() throws RetryFailedException, InterruptedException {
        Map<String, AttributeValue> attributeValueMap = indexInput.getAttributes().entrySet()
                .stream()
                .filter((entry) -> entry.getValue() != null)
                .filter((entry) -> StringUtils.isNotBlank(entry.getValue().toString()))
                .collect(Collectors.toMap(
                        e -> e.getKey().toString(),
                        e -> {
                            try {
                                switch (e.getKey().getType()) {
                                    case LONG:
                                    case DOUBLE:
                                        return new AttributeValue().withN(e.getValue().toString());

                                    default:
                                        String serializedData = null;

                                        // use objectMapper for everything but strings because it
                                        // adds quotes to raw strings
                                        if (e.getValue().getClass() != String.class) {
                                            serializedData = objectMapper.writeValueAsString(e.getValue());
                                        } else {
                                            serializedData = (String) e.getValue();
                                        }

                                        return new AttributeValue().withS(serializedData);
                                }
                            } catch (Exception ex) {
                                logger.error("Couldn't serialize data to index.", ex);
                                return null;
                            }
                        }
                ));

        String tableName = config.getString(TABLE_NAME_CONFIG_KEY);

        PutItemRequest putItemRequest = new PutItemRequest()
                .withTableName(tableName)
                .withItem(attributeValueMap)
                .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);

        final PutItemResult putItemResult = retry(() -> dynamoDBClient.putItem(putItemRequest));

        logger.info("Consumed table capacity: " + putItemResult.getConsumedCapacity().getCapacityUnits());
    }

    private void writeToRDBMS() throws SQLException, NoSuchAlgorithmException {
        Connection connection = dataSource.getConnection();
        connection.prepareStatement("use widow;").execute();

        final PreparedStatement statement = connection.prepareStatement(
                "insert into " +
                "page_data(time_accessed," +
                        "  original_url_hash," +
                        "  original_url," +
                        "  load_time_millis," +
                        "  status_code," +
                        "  headers," +
                        "  response_size," +
                        "  content_size," +
                        "  page_content_ref," +
                        "  title," +
                        "  out_links," +
                        "  css_links," +
                        "  img_links," +
                        "  js_links," +
                        "  size_with_assets)" +
                "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        statement.setLong(  1 , Long.valueOf(indexInput.getAttribute(PageAttribute.TIME_ACCESSED).toString()));
        statement.setString(2 , DigestUtils.shaHex(indexInput.getAttribute(PageAttribute.ORIGINAL_URL).toString()));
        statement.setString(3 , indexInput.getAttribute(PageAttribute.ORIGINAL_URL).toString());
        statement.setLong(  4 , Double.valueOf(indexInput.getAttribute(PageAttribute.LOAD_TIME_MILLIS).toString()).longValue());
        statement.setInt(   5 , Integer.valueOf(indexInput.getAttribute(PageAttribute.STATUS_CODE).toString()));
        statement.setString(6 , indexInput.getAttribute(PageAttribute.HEADERS).toString());
        statement.setInt(   7 , Integer.valueOf(indexInput.getAttribute(PageAttribute.RESPONSE_SIZE).toString()));
        statement.setInt(   8 , Integer.valueOf(indexInput.getAttribute(PageAttribute.CONTENT_SIZE).toString()));
        statement.setString(9 , indexInput.getAttribute(PageAttribute.PAGE_CONTENT_REF).toString());
        statement.setString(10, indexInput.getAttribute(PageAttribute.TITLE).toString());
        statement.setString(11, indexInput.getAttribute(PageAttribute.OUT_LINKS).toString());
        statement.setString(12, indexInput.getAttribute(PageAttribute.CSS_LINKS).toString());
        statement.setString(13, indexInput.getAttribute(PageAttribute.IMG_LINKS).toString());
        statement.setString(14, indexInput.getAttribute(PageAttribute.JS_LINKS).toString());
        statement.setInt(   15, Integer.valueOf(indexInput.getAttribute(PageAttribute.SIZE_WITH_ASSETS).toString()));

        statement.executeUpdate();
    }
}
