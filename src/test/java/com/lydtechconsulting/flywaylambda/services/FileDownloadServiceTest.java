package com.lydtechconsulting.flywaylambda.services;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpGet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;

import static java.nio.file.Files.readString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileDownloadServiceTest {
    private static final String BUCKET_NAME = "myBucket";
    private static final String FILE_CONTENTS = "myFileContents";
    
    private FileDownloadService fileDownloadService = new FileDownloadService();
    
    @Test
    public void should_copy_files_from_s3(@TempDir Path tempDir) throws IOException {
        //create a mock for the s3 client which will mimic a single file being available in the bucket
        AmazonS3 s3ClientMock = createAmazonS3ClientMock();

        //call the service to copy all files from s3 to the temp dir
        fileDownloadService.copy(mock(LambdaLogger.class), s3ClientMock, BUCKET_NAME, tempDir.toString());

        //assert the correct file has been copied to the temp directory
        String file = readString(Path.of(tempDir.toString(), "key1"), Charset.defaultCharset());
        assertEquals(FILE_CONTENTS, file);
    }
    
    private AmazonS3 createAmazonS3ClientMock() {
        AmazonS3 s3ClientMock = mock(AmazonS3.class);
        
        //pretend there is 1 file in the bucket
        ListObjectsV2Result listObjectsResult = mock(ListObjectsV2Result.class);
        S3ObjectSummary s3ObjectSummary = new S3ObjectSummary();
        s3ObjectSummary.setKey("key1");
        when(listObjectsResult.getObjectSummaries()).thenReturn(Arrays.asList(s3ObjectSummary));
        when(s3ClientMock.listObjectsV2(BUCKET_NAME)).thenReturn(listObjectsResult);

        //return the contents of that file
        S3Object s3Object = mock(S3Object.class);
        InputStream targetStream = IOUtils.toInputStream(FILE_CONTENTS, Charset.defaultCharset());
        when(s3Object.getObjectContent()).thenReturn(new S3ObjectInputStream(targetStream, new HttpGet()));
        when(s3ClientMock.getObject(BUCKET_NAME, "key1")).thenReturn(s3Object);
        return s3ClientMock;
    }

}
