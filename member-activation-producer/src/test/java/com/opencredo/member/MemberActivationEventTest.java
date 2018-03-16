package com.opencredo.member;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class MemberActivationEventTest {


    @Test
    public void iCanCreateaMemberActiationEventsWithActivationTimestamp(){

        LocalDateTime activationDateTime = LocalDateTime.now();

        String activationDateTimeAsString = activationDateTime.toString();

        log.info("Activation date {}",activationDateTimeAsString);

        MemberActivationEvent memberActivationEvent = new MemberActivationEvent(activationDateTimeAsString);

        assertEquals(activationDateTimeAsString,memberActivationEvent.getActivationTimestamp());


    }

}