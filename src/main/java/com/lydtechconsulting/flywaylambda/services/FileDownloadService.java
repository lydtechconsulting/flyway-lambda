package com.lydtechconsulting.flywaylambda.services;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class FileDownloadService {
    /**
     * Given an s3 bucket name, download all files into the specified location
     * @param logger use this for logging
     * @param s3Client s3client to use for list/get
     * @param s3BucketName name of s3 bucket to pull files from
     * @param flywayScriptsLocation path to download files to
     */
    public void copy(LambdaLogger logger, AmazonS3 s3Client, String s3BucketName, String flywayScriptsLocation) {
        logger.log("Will copy files from " + s3BucketName + " to " + flywayScriptsLocation);

        ListObjectsV2Result result = s3Client.listObjectsV2(s3BucketName);
        List<S3ObjectSummary> objects = result.getObjectSummaries();
        for (S3ObjectSummary os : objects) {
            logger.log("* " + os.getKey());
            S3Object o = s3Client.getObject(s3BucketName, os.getKey());
            S3ObjectInputStream s3is = o.getObjectContent();
            try {
                FileOutputStream fos = new FileOutputStream(new File(flywayScriptsLocation, os.getKey()));

                byte[] read_buf = new byte[1024];
                int read_len = 0;
                while ((read_len = s3is.read(read_buf)) > 0) {
                    fos.write(read_buf, 0, read_len);
                }
                s3is.close();
                fos.close();
            } catch (Exception e) {
                throw new RuntimeException("Problem downloading file", e);
            }
        }
    }
}
