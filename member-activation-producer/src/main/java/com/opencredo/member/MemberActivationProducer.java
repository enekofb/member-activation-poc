/*
 * Copyright 2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Amazon Software License (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://aws.amazon.com/asl/
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.opencredo.member;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.kinesis.producer.*;
import com.amazonaws.services.kinesis.producer.sample.SampleConsumer;
import com.amazonaws.services.kinesis.producer.sample.Utils;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The Kinesis Producer Library (KPL) excels at handling large numbers of small
 * logical records by combining multiple logical records into a single Kinesis
 * record.
 * 
 * <p>
 * In this sample we'll be putting a monotonically increasing sequence number in
 * each logical record, and then padding the record to 128 bytes long. The
 * consumer will then check that all records are received correctly by verifying
 * that there are no gaps in the sequence numbers.
 * 
 * <p>
 * We will distribute the records evenly across all shards by using a random
 * explicit hash key.
 * 
 * <p>
 * To prevent the consumer from being confused by data from multiple runs of the
 * producer, each record also carries the time at which the producer started.
 * The consumer will reset its state whenever it detects a new, larger
 * timestamp. We will place the timestamp in the partition key. This does not
 * affect the random distribution of records across shards since we've set an
 * explicit hash key.
 * 
 * @see SampleConsumer
 * @author chaodeng
 *
 */
@Slf4j
@Value
public class MemberActivationProducer {

    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(1);
    
    /**
     * Timestamp we'll attach to every record
     */
    private static final String TIMESTAMP = Long.toString(System.currentTimeMillis());
    
    /**
     * Change these to try larger or smaller records.
     */
    private static final int DATA_SIZE = 128;
    
    /**
     * Put records for this number of seconds before exiting.
     */
    private static final int SECONDS_TO_RUN = 5;
    
    /**
     * Put this number of records per second.
     * 
     * Because multiple logical records are combined into each Kinesis record,
     * even a single shard can handle several thousand records per second, even
     * though there is a limit of 1000 Kinesis records per shard per second.
     * 
     * If a shard gets throttled, the KPL will continue to retry records until
     * either they succeed or reach a TTL set in the KPL's configuration, at
     * which point the KPL will return failures for those records.
     * 
     * @see {@link KinesisProducerConfiguration#setRecordTtl(long)}
     */
    private static final int RECORDS_PER_SECOND = 2000;

    /**
     * Change this to the region you are using.
     */
    public static final String REGION = "eu-west-1";


    /**
     * Change this to your stream name.
     */
    private final String streamName;

    private final KinesisProducer producer;

    private final UserRecordAdapter userRecordAdapter;


    public MemberActivationProducer(String streamName){
        this.streamName = streamName;
        this.producer = getKinesisProducer();
        this.userRecordAdapter = new UserRecordAdapter();
    }


    /**
     * Here'll walk through some of the config options and create an instance of
     * KinesisProducer, which will be used to put records.
     * 
     * @return KinesisProducer instance used to put records.
     */
    private KinesisProducer getKinesisProducer() {
        // There are many configurable parameters in the KPL. See the javadocs
        // on each each set method for details.
        KinesisProducerConfiguration config = new KinesisProducerConfiguration();
        
        // You can also load config from file. A sample properties file is
        // included in the project folder.
        // KinesisProducerConfiguration config =
        //     KinesisProducerConfiguration.fromPropertiesFile("default_config.properties");
        
        // If you're running in EC2 and want to use the same Kinesis region as
        // the one your instance is in, you can simply leave out the region
        // configuration; the KPL will retrieve it from EC2 metadata.
        config.setRegion(REGION);
        
        // You can pass credentials programmatically through the configuration,
        // similar to the AWS SDK. DefaultAWSCredentialsProviderChain is used
        // by default, so this configuration can be omitted if that is all
        // that is needed.
        config.setCredentialsProvider(new DefaultAWSCredentialsProviderChain());
        
        // The maxConnections parameter can be used to control the degree of
        // parallelism when making HTTP requests. We're going to use only 1 here
        // since our throughput is fairly low. Using a high number will cause a
        // bunch of broken pipe errors to show up in the logs. This is due to
        // idle connections being closed by the server. Setting this value too
        // large may also cause request timeouts if you do not have enough
        // bandwidth.
        config.setMaxConnections(1);
        
        // Set a more generous timeout in case we're on a slow connection.
        config.setRequestTimeout(60000);
        
        // RecordMaxBufferedTime controls how long records are allowed to wait
        // in the KPL's buffers before being sent. Larger values increase
        // aggregation and reduces the number of Kinesis records put, which can
        // be helpful if you're getting throttled because of the records per
        // second limit on a shard. The default value is set very low to
        // minimize propagation delay, so we'll increase it here to get more
        // aggregation.
        config.setRecordMaxBufferedTime(15000);

        // If you have built the native binary yourself, you can point the Java
        // wrapper to it with the NativeExecutable option. If you want to pass
        // environment variables to the executable, you can either use a wrapper
        // shell script, or set them for the Java process, which will then pass
        // them on to the child process.
        // config.setNativeExecutable("my_directory/kinesis_producer");
        
        // If you end up using the default configuration (a Configuration instance
        // without any calls to set*), you can just leave the config argument
        // out.
        //
        // Note that if you do pass a Configuration instance, mutating that
        // instance after initializing KinesisProducer has no effect. We do not
        // support dynamic re-configuration at the moment.
        KinesisProducer producer = new KinesisProducer(config);
        
        return producer;
    }


//    /**
//     * Executes a function N times per second for M seconds with a
//     * ScheduledExecutorService. The executor is shutdown at the end. This is
//     * more precise than simply using scheduleAtFixedRate.
//     *
//     * @param exec
//     *            Executor
//     * @param task
//     *            Task to perform
//     * @param counter
//     *            Counter used to track how many times the task has been
//     *            executed
//     * @param durationSeconds
//     *            How many seconds to run for
//     * @param ratePerSecond
//     *            How many times to execute task per second
//     */
//    private static void executeAtTargetRate(
//            final ScheduledExecutorService exec,
//            final Runnable task,
//            final AtomicLong counter,
//            final int durationSeconds,
//            final int ratePerSecond) {
//        exec.scheduleWithFixedDelay(new Runnable() {
//            final long startTime = System.nanoTime();
//
//            @Override
//            public void run() {
//                double secondsRun = (System.nanoTime() - startTime) / 1e9;
//                double targetCount = Math.min(durationSeconds, secondsRun) * ratePerSecond;
//
//                while (counter.get() < targetCount) {
//                    counter.getAndIncrement();
//                    try {
//                        task.run();
//                    } catch (Exception e) {
//                        log.error("Error running task", e);
//                        System.exit(1);
//                    }
//                }
//
//                if (secondsRun >= durationSeconds) {
//                    exec.shutdown();
//                }
//            }
//        }, 0, 1, TimeUnit.MILLISECONDS);
//    }
//
//    // The monotonically increasing sequence number we will put in the data of each record
//    final AtomicLong sequenceNumber = new AtomicLong(0);
//
//    // The number of records that have finished (either successfully put, or failed)
//    final AtomicLong completed = new AtomicLong(0);
//


    public UserRecordResult publish(MemberActivationEvent memberActivationEvent) throws InterruptedException {

        try{
            final UserRecord memberActivationEventAsUserRecord = toUserRecord(memberActivationEvent);

            ListenableFuture<UserRecordResult> f =
                    producer.addUserRecord(memberActivationEventAsUserRecord);

            return producer.addUserRecord(memberActivationEventAsUserRecord).get();
        } catch (ExecutionException e) {
            log.error("Could not send event",e);
        } finally {
            log.info("Waiting for remaining puts to finish...");
            producer.flushSync();
            log.info("All records complete.");

            // This kills the child process and shuts down the threads managing it.
            producer.destroy();
            log.info("Finished.");

        }

        return null;
    }

    protected UserRecord toUserRecord(MemberActivationEvent memberActivationEvent) {
        return userRecordAdapter.
                fromMemberActivationEvent(memberActivationEvent);
    }


    private class UserRecordAdapter {

        public UserRecord fromMemberActivationEvent(MemberActivationEvent memberActivationEvent){

            String partitionKey = "1";
            String randomExplicitHashKey = Utils.randomExplicitHashKey();

            UserRecord memberActivationAsUserRecord = new UserRecord();
            memberActivationAsUserRecord.setStreamName(getStreamName());
            memberActivationAsUserRecord.setPartitionKey(partitionKey);
            memberActivationAsUserRecord.setData(ByteBuffer.wrap(memberActivationEvent.toJsonAsBytes()));
            memberActivationAsUserRecord.setExplicitHashKey(randomExplicitHashKey);
            return memberActivationAsUserRecord;
        }
    }
}
