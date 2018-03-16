package com.opencredo.member;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class MemberActivationDynamodbClientTest {

    MemberActivationViewProducer memberActivationViewProducer = new MemberActivationViewProducer();

    @Test
    public void canConnectToMemberActivationTable(){
        boolean memberConnectedExists = memberActivationViewProducer.checkMemberActivationsByActivationTime();
        assertTrue(memberConnectedExists);
    }

    @Test
    public void canWritetToMemberActivationEvent(){
        MemberActivationEvent memberActivationEvent = createMemberActivationEvent();
        boolean memberActivationWritten = memberActivationViewProducer.writeMemberActivationEvent(memberActivationEvent);
        assertTrue(memberActivationWritten);
    }

    private MemberActivationEvent createMemberActivationEvent() {
        LocalDateTime activationDateTime = LocalDateTime.now();
        String activationDateTimeAsString = activationDateTime.toString();
        log.info("Activation date {}",activationDateTimeAsString);
        return new MemberActivationEvent(activationDateTimeAsString);
    }


    @Test
    public void canGetAllMemberActivationEventByActivationkey(){
        String activationTimePk = LocalDate.now().toString();
        ItemCollection<QueryOutcome> memberActivationWritten = memberActivationViewProducer.getAllMemberActivationEventByPartitionKey(activationTimePk);
        assertTrue(memberActivationWritten.getAccumulatedItemCount() > 0 );
    }

}
