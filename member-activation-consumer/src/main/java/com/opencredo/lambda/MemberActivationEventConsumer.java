package com.opencredo.lambda;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.opencredo.member.MemberActivationEvent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class MemberActivationEventConsumer {

    private AmazonS3 s3Client;

    public MemberActivationEventConsumer(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    private static String BUCKET_NAME = "gg-member-activations-tests";

    public PutObjectResult postEventToS3(MemberActivationEvent data) {
        LocalDateTime activationTimestamp = data.getActivationTimestamp();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd/HH/");

        String fileName = activationTimestamp.format(formatter) + data.getActivationTimestamp().toString() + "-" + UUID.randomUUID();

        return s3Client.putObject(BUCKET_NAME, fileName, data.toJson());
    }
}
