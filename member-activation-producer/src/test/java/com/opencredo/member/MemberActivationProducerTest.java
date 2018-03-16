package com.opencredo.member;


import com.amazonaws.services.kinesis.producer.UserRecord;
import com.amazonaws.services.kinesis.producer.UserRecordResult;
import com.google.common.util.concurrent.FutureCallback;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class MemberActivationProducerTest {

    private final static String MEMBER_ACTIVATION_STREAM_NAME = "activations-test";

    private MemberActivationProducer memberActivationProducer = new MemberActivationProducer(MEMBER_ACTIVATION_STREAM_NAME);

    @Test
    public void iCanPublishMemberActivationEvents() throws InterruptedException {

        MemberActivationEvent memberActivationEvent = createMemmberActivationEvent();

        UserRecordResult published = memberActivationProducer.publish(memberActivationEvent);

        assertEquals(true, published.isSuccessful());

    }

    private MemberActivationEvent createMemmberActivationEvent() {
        LocalDateTime activationDateTime = LocalDateTime.now();
        String activationDateTimeAsString = activationDateTime.toString();
        log.info("Activation date {}",activationDateTimeAsString);
        return new MemberActivationEvent(activationDateTimeAsString);
    }

    @Test
    public void iCanCreateUserRecordFromMemberActivationEvent(){
        MemberActivationEvent memberActivationEvent = createMemmberActivationEvent();
        UserRecord userRecord = memberActivationProducer.toUserRecord(memberActivationEvent);
        assertThat(userRecord.getData(),is(notNullValue()));
        assertThat(userRecord.getStreamName(),equalTo(memberActivationProducer.getStreamName()));
    }




}