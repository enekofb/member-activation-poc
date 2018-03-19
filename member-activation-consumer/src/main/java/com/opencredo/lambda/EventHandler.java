package com.opencredo.lambda;

import com.amazonaws.handlers.RequestHandler2;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.opencredo.member.MemberActivationEvent;

import com.amazonaws.services.lambda.runtime.events.KinesisEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EventHandler extends RequestHandler2 {

    private MemberActivationEventConsumer memberActivationEventConsumer;

    public List<PutObjectResult> handle(KinesisEvent event) throws IOException {
        AmazonS3ClientBuilder s3Client = AmazonS3ClientBuilder.standard();
        s3Client.setRegion("eu-west-1");

        memberActivationEventConsumer = new MemberActivationEventConsumer(s3Client.build());
        List<PutObjectResult> results = new ArrayList<>();

        for (KinesisEvent.KinesisEventRecord record: event.getRecords()) {
            System.out.println("record toString: " + record.toString());

            MemberActivationEvent memberActivationEvent = MemberActivationEvent.fromBytes(record.getKinesis().getData().array());
            PutObjectResult putObjectResult = memberActivationEventConsumer.postEventToS3(memberActivationEvent);
            results.add(putObjectResult);
        }
        return results;
    }
}
