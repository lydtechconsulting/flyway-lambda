package com.lydtechconsulting.flywaylambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.lydtechconsulting.flywaylambda.services.FileDownloadService;
import com.lydtechconsulting.flywaylambda.services.FlywayMigrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

import java.util.HashMap;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
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
    public void should_run_successfully() throws Exception {
        Context context = mock(Context.class);
        LambdaLogger logger = mock(LambdaLogger.class);
        when(context.getLogger()).thenReturn(logger);

        HashMap<String, String> event = new HashMap<>();
        event.put("bucket_name", "myBucket");
        event.put("secret_name", "mySecretName");

        String response = withEnvironmentVariable("AWS_REGION", "eu-west-1")
                .execute(() -> flywayHandler.handleRequest(event, context));


        assertEquals("200 OK", response);

        verify(fileDownloadService).copy(eq(logger), any(S3Client.class), eq("myBucket"), matches("/tmp/sqlFiles_.*"));
        verify(flywayMigrationService).performMigration(eq(logger), any(SecretsManagerClient.class), matches("^filesystem:///tmp/sqlFiles_.*"), matches("mySecretName"));
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
        event.put("secret_name", "mySecretName");

        try {
            //No region as an env var
            flywayHandler.handleRequest(event, context);
            fail("expected nullPointerException");
        } catch (NullPointerException e) {
            assertEquals("AWS_REGION expected to be set", e.getMessage());
        }
        
        verifyNoInteractions(fileDownloadService);
        verifyNoInteractions(flywayMigrationService);
    }
}
