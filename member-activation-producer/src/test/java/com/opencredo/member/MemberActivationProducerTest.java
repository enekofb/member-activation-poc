package com.opencredo.member;


import com.amazonaws.services.kinesis.producer.Attempt;
import com.amazonaws.services.kinesis.producer.KinesisProducer;
import com.amazonaws.services.kinesis.producer.UserRecordFailedException;
import com.amazonaws.services.kinesis.producer.UserRecordResult;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.opencredo.member.MemberActivationProducer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@RunWith(MockitoJUnitRunner.class)
public class MemberActivationProducerTest {

    private MemberActivationProducer memberActivationProducer = new MemberActivationProducer();


    @Test
    public void iCanProduceMemberActivationEvents(){


    }

//    @Test
//    public void iCanPublishToActivationTestStream() throws InterruptedException {
//        SampleProducer.publish();
//    }

}