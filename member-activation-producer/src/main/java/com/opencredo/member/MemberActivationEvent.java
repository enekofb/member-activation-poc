package com.opencredo.member;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.Value;

import java.io.IOException;
import java.nio.ByteBuffer;

@Value
public class MemberActivationEvent {

    private final String activationTimestamp;


    private final static ObjectMapper JSON = new ObjectMapper();

    static {
        JSON.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    //TODO I should go to a userRecordAdapter
    public String getPartitionKey() {

        return "1";

    }

    public byte[] toJsonAsBytes() {
        try {
            return JSON.writeValueAsBytes(this);
        } catch (IOException e) {
            return null;
        }
    }



}
