package com.opencredo.member;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

@Value
@Slf4j
public class MemberActivationViewProducer {

    public static final String MEMBER_ACTIVATIONS_BY_ACTIVATION_TIME = "memberActivationsByActivationTime";

    private final DynamoDB dynamoDB;
    private final Table memberActivationsTable;
    private final AmazonDynamoDB client;

    public MemberActivationViewProducer() {
        this.client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.EU_WEST_1)
                .build();
        this.dynamoDB = new DynamoDB(client);
        //TODO revisit this
        this.memberActivationsTable = this.dynamoDB.getTable(MEMBER_ACTIVATIONS_BY_ACTIVATION_TIME);
    }

    public boolean checkMemberActivationsByActivationTime() {
        return !this.client.listTables(MEMBER_ACTIVATIONS_BY_ACTIVATION_TIME).getTableNames().
                isEmpty();
    }

    public boolean writeMemberActivationEvent(MemberActivationEvent memberActivationEvent) {

        Item memberActivationEventItem = createItemFromMemberActivationEvent(memberActivationEvent);
        PutItemOutcome putItemOutcome = this.memberActivationsTable.putItem(memberActivationEventItem);
        return putItemOutcome.getPutItemResult().getSdkHttpMetadata().getHttpStatusCode() == 200;

    }

    private Item createItemFromMemberActivationEvent(MemberActivationEvent memberActivationEvent) {

        LocalDateTime formatDateTime = LocalDateTime.parse(memberActivationEvent.getActivationTimestamp(),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        String activationTimePk = formatDateTime.toLocalDate().toString();

        Item memberActivationItem = new Item()
                .withPrimaryKey("activationTime", activationTimePk)
                .withString("activationTimestamp", memberActivationEvent.getActivationTimestamp());

        return memberActivationItem;

    }

    public ItemCollection<QueryOutcome> getAllMemberActivationEventByPartitionKey(String activationTime) {


        QuerySpec spec = new QuerySpec().withKeyConditionExpression("activationTime = :v_id")
                .withValueMap(new ValueMap().withString(":v_id", activationTime));

        ItemCollection<QueryOutcome> items = memberActivationsTable.query(spec);


        Iterator<Item> iterator = items.iterator();
        while (iterator.hasNext()) {
            log.info("Found {}", iterator.next().toJSONPretty());

        }
        return items;

    }
}
