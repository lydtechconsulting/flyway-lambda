package com.lydtechconsulting.flywaylambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.AmazonS3;
import com.lydtechconsulting.flywaylambda.services.FileDownloadService;
import com.lydtechconsulting.flywaylambda.services.FlywayMigrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlywayHandlerTest {
    @Mock
    private FlywayMigrationService flywayMigrationService;

    @Mock
    private FileDownloadService fileDownloadService;

    @InjectMocks
    private FlywayHandler flywayHandler;
    
    @BeforeEach
    public void setUp() {
        System.clearProperty("AWS_REGION");
    }

    @Test
    public void should_run_successfully() {
        Context context = mock(Context.class);
        LambdaLogger logger = mock(LambdaLogger.class);
        when(context.getLogger()).thenReturn(logger);

        HashMap<String, String> event = new HashMap<>();
        event.put("bucket_name", "myBucket");
        System.setProperty("AWS_REGION", "eu-west-1");

        String response = flywayHandler.handleRequest(event, context);

        assertEquals("200 OK", response);

        verify(fileDownloadService).copy(eq(logger), ArgumentMatchers.any(AmazonS3.class), eq("myBucket"), matches("/tmp/sqlFiles_.*"));
        verify(flywayMigrationService).performMigration(eq(logger), matches("^filesystem:///tmp/sqlFiles_.*"));
    }

    @Test
    public void should_fail_when_bucket_name_not_present() {
        Context context = mock(Context.class);
        when(context.getLogger()).thenReturn(mock(LambdaLogger.class));

        //no bucket_name in event
        try {
            flywayHandler.handleRequest(new HashMap<>(), context);
            fail("expected nullPointerException");
        } catch (NullPointerException e) {
            assertEquals("event must have bucket_name field", e.getMessage());
        }

        verifyNoInteractions(fileDownloadService);
        verifyNoInteractions(flywayMigrationService);
    }

    @Test
    public void should_fail_when_region_not_present() {
        Context context = mock(Context.class);
        LambdaLogger logger = mock(LambdaLogger.class);
        when(context.getLogger()).thenReturn(logger);

        HashMap<String, String> event = new HashMap<>();
        event.put("bucket_name", "myBucket");

        try {
            //No region as a system property
            flywayHandler.handleRequest(event, context);
            fail("expected nullPointerException");
        } catch (NullPointerException e) {
            assertEquals("AWS_REGION expected to be set", e.getMessage());
        }
        
        verifyNoInteractions(fileDownloadService);
        verifyNoInteractions(flywayMigrationService);
    }
    
    @Test
    public void should_fail_when_region_invalid_region_present() {
        Context context = mock(Context.class);
        LambdaLogger logger = mock(LambdaLogger.class);
        when(context.getLogger()).thenReturn(logger);

        HashMap<String, String> event = new HashMap<>();
        event.put("bucket_name", "myBucket");

        try {
            System.setProperty("AWS_REGION", "invalid");
            flywayHandler.handleRequest(event, context);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Cannot create enum from invalid value!", e.getMessage());
        }

        verifyNoInteractions(fileDownloadService);
        verifyNoInteractions(flywayMigrationService);
    }
}
