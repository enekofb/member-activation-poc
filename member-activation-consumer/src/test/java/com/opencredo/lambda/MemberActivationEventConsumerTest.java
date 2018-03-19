package com.opencredo.lambda;

import com.amazonaws.services.s3.AmazonS3;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class MemberActivationEventConsumerTest {

    @Mock
    private AmazonS3 amazonS3;

    @Test
    public void postsEventToS3() {
    }
}