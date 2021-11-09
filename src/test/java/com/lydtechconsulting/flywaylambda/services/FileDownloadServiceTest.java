package com.lydtechconsulting.flywaylambda.services;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpGet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;

import static java.nio.file.Files.readString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileDownloadServiceTest {
    private static final String BUCKET_NAME = "myBucket";
    private static final String FILE_CONTENTS = "myFileContents";
    
    private FileDownloadService fileDownloadService = new FileDownloadService();
    
    @Test
    public void should_copy_files_from_s3(@TempDir Path tempDir) throws IOException {
        //create a mock for the s3 client which will mimic a single file being available in the bucket
        S3Client s3ClientMock = createAmazonS3ClientMock();

        //call the service to copy all files from s3 to the temp dir
        fileDownloadService.copy(mock(LambdaLogger.class), s3ClientMock, BUCKET_NAME, tempDir.toString());

        //assert the correct file has been copied to the temp directory
        String file = readString(Path.of(tempDir.toString(), "key1"), Charset.defaultCharset());
        assertEquals(FILE_CONTENTS, file);
    }
    
    private S3Client createAmazonS3ClientMock() {
        S3Client s3ClientMock = mock(S3Client.class);
        
        //pretend there is 1 file in the bucket
        when(s3ClientMock.listObjects(any(ListObjectsRequest.class))).thenReturn(ListObjectsResponse
                .builder()
                .contents(S3Object.builder()
                        .key("key1")
                        .build())
                .build());

        //return the contents of that file
        when(s3ClientMock.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), FILE_CONTENTS.getBytes()));
        return s3ClientMock;
    }

}
