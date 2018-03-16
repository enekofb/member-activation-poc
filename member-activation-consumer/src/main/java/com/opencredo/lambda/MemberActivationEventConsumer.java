package com.opencredo.lambda;

import com.amazonaws.services.kinesis.model.Record;
import com.amazonaws.services.s3.model.S3Event;

import javax.naming.Context;
import java.util.List;

public class MemberActivationEventConsumer {

    public void handle(List<Record> records) {
        System.out.println("HELLO");
    }

}
