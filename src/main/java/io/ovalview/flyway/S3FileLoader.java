package io.ovalview.flyway;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

public class S3FileLoader {
    public void copy(String s3BucketName, String destination) {
        System.out.println("Will copy files from " + s3BucketName + " to " + destination);

        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.EU_WEST_2).build();
        ListObjectsV2Result result = s3.listObjectsV2(s3BucketName);
        List<S3ObjectSummary> objects = result.getObjectSummaries();
        for (S3ObjectSummary os : objects) {
            System.out.println("* " + os.getKey());
            S3Object o = s3.getObject(s3BucketName, os.getKey());
            S3ObjectInputStream s3is = o.getObjectContent();
            try {
                FileOutputStream fos = new FileOutputStream(new File(destination, os.getKey()));

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
