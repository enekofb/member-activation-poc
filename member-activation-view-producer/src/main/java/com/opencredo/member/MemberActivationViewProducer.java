package com.opencredo.member;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Value
@Slf4j
public class MemberActivationViewProducer {

    public static final String MEMBER_ACTIVATIONS_BY_ACTIVATION_TIME = "memberActivationsByActivationTime";

    private final DynamoDB dynamoDB;
    private final Table memberActivationsTable;
    private final AmazonDynamoDB client;

    public MemberActivationViewProducer(){
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
                .withString("activationTimestamp",memberActivationEvent.getActivationTimestamp());

        return memberActivationItem;

    }
}
