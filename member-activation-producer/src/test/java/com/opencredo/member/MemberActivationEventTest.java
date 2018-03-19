package com.opencredo.member;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class MemberActivationEventTest {


    private MemberActivationEvent createMemberActivationEvent() {
        LocalDateTime activationDateTime = LocalDateTime.now();

        String activationDateTimeAsString = activationDateTime.toString();

        log.info("Activation date {}", activationDateTimeAsString);

        return new MemberActivationEvent(activationDateTime);

    }

    @Test
    public void iCanCreateMemberActivationEventsWithActivationTimestamp() {

        LocalDateTime activationDateTime = LocalDateTime.now();

        String activationDateTimeAsString = activationDateTime.toString();

        log.info("Activation date {}", activationDateTimeAsString);

        MemberActivationEvent memberActivationEvent = new MemberActivationEvent(activationDateTime);

        assertEquals(activationDateTime, memberActivationEvent.getActivationTimestamp());


    }


    @Test
    public void iCanSerializeEventAsJson() {

        MemberActivationEvent memberActivationEvent = createMemberActivationEvent();
        assertTrue(memberActivationEvent.toJsonAsBytes().length > 0);

    }


}