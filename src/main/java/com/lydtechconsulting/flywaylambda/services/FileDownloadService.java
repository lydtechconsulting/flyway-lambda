package com.lydtechconsulting.flywaylambda.services;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

public class FileDownloadService {
    /**
     * Given an s3 bucket name, download all files into the specified location
     * @param logger use this for logging
     * @param s3Client s3client to use for list/get
     * @param s3BucketName name of s3 bucket to pull files from
     * @param flywayScriptsLocation path to download files to
     */
    public void copy(LambdaLogger logger, S3Client s3Client, String s3BucketName, String flywayScriptsLocation) {
        logger.log("Will copy files from " + s3BucketName + " to " + flywayScriptsLocation);

        ListObjectsRequest request = ListObjectsRequest
                .builder()
                .bucket(s3BucketName)
                .build();

        ListObjectsResponse response = s3Client.listObjects(request);
        List<S3Object> objects = response.contents();
        for (S3Object obj : objects) {
            String key = obj.key();
            logger.log("* " + key);
            GetObjectRequest objectRequest = GetObjectRequest
                    .builder()
                    .key(key)
                    .bucket(s3BucketName)
                    .build();
            ResponseBytes<GetObjectResponse> objectAsBytes = s3Client.getObjectAsBytes(objectRequest);
            InputStream inputStream = objectAsBytes.asInputStream();
            try {
                FileOutputStream fos = new FileOutputStream(new File(flywayScriptsLocation, key));

                byte[] read_buf = new byte[1024];
                int read_len = 0;
                while ((read_len = inputStream.read(read_buf)) > 0) {
                    fos.write(read_buf, 0, read_len);
                }
                inputStream.close();
                fos.close();
            } catch (Exception e) {
                throw new RuntimeException("Problem downloading file", e);
            }
        }
    }
}
