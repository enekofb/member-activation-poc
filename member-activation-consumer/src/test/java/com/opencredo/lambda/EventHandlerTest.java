package com.opencredo.lambda;

import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.member.MemberActivationEvent;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class EventHandlerTest {

    private EventHandler eventHandler;

    @Before
    public void init() {
        eventHandler = new EventHandler();
    }

    @Test
    public void testFromText() throws IOException {
        String kinesisEventString = "    {\n" +
                "      \"records\": [\n" +
                "        {\n" +
                "          \"eventID\": \"shardId-000000000000:49545115243490985018280067714973144582180062593244200961\",\n" +
                "          \"eventVersion\": \"1.0\",\n" +
                "          \"kinesis\": {\n" +
                "            \"approximateArrivalTimestamp\": 1428537600,\n" +
                "            \"partitionKey\": \"partitionKey-3\",\n" +
                "            \"data\": \"eyJhY3RpdmF0aW9uVGltZXN0YW1wIjogIjIwMTgtMDMtMTZUMTE6MjQ6NTQuOTcxIn0=\",\n" +
                "            \"kinesisSchemaVersion\": \"1.0\",\n" +
                "            \"sequenceNumber\": \"49545115243490985018280067714973144582180062593244200961\"\n" +
                "          },\n" +
                "          \"invokeIdentityArn\": \"arn:aws:iam::EXAMPLE\",\n" +
                "          \"eventName\": \"aws:kinesis:record\",\n" +
                "          \"eventSourceARN\": \"arn:aws:kinesis:EXAMPLE\",\n" +
                "          \"eventSource\": \"aws:kinesis\",\n" +
                "          \"awsRegion\": \"eu-west-1\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }";

        ObjectMapper JSON = new ObjectMapper();
        KinesisEvent kinesisEvent = JSON.readValue(kinesisEventString, KinesisEvent.class);
        List<PutObjectResult> results = eventHandler.handle(kinesisEvent);

        assertThat(results.size(), is(1));
        assertThat(results.get(0).getContentMd5(), is("ZEp7Ph1x5DAv8BTl3tGJmA=="));
    }

    @Test
    public void handleSendsEachRecordToS3() throws Exception {
        KinesisEvent kinesisEvent = new KinesisEvent();
        KinesisEvent.KinesisEventRecord record = new KinesisEvent.KinesisEventRecord();
        LocalDateTime timestamp = LocalDateTime.now();
        MemberActivationEvent event = new MemberActivationEvent(timestamp);
        record.getKinesis().setData(stringToByteBuffer(event.toString(), Charset.defaultCharset()));
        kinesisEvent.setRecords(Arrays.asList(record, record));

        List<PutObjectResult> results = eventHandler.handle(kinesisEvent);

        assertThat(results.size(), is(1));
    }

    private static ByteBuffer stringToByteBuffer(String msg, Charset charset){
        return ByteBuffer.wrap(msg.getBytes(charset));
    }
}