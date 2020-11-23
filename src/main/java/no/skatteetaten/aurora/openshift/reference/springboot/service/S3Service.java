package no.skatteetaten.aurora.openshift.reference.springboot.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import no.skatteetaten.aurora.openshift.reference.springboot.service.dto.S3Configuration;
import no.skatteetaten.aurora.openshift.reference.springboot.service.dto.S3Properties;
import no.skatteetaten.aurora.openshift.reference.springboot.service.exceptions.ObjectStorageException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3Service {
    private S3Configuration defaultS3Configuration;
    private S3Configuration otherS3Configuration;

    public S3Service(
        S3Configuration s3Configuration,
        @Qualifier("otherArea") S3Configuration otherS3Configuration
    ) {
        this.defaultS3Configuration = s3Configuration;
        this.otherS3Configuration = otherS3Configuration;
    }

    public void putFileContent(String keyName, String content, Boolean useDefaultBucketObjectArea) {
        S3Configuration s3Config = useDefaultBucketObjectArea ? defaultS3Configuration : otherS3Configuration;
        S3Properties.S3Bucket s3Bucket = s3Config.getBucket();
        S3Client s3Client = s3Config.getS3Client();

        try {
            var request = PutObjectRequest.builder()
                .bucket(s3Bucket.getBucketName())
                .key(getKeyName(s3Bucket, keyName)).build();

            s3Client.putObject(request, RequestBody.fromString(content));
        } catch (Exception e) {
            throw new ObjectStorageException("An error occurred while communicating with S3 storage", e);
        }
    }

    public String getFileContent(String keyName, Boolean useDefaultBucketObjectArea) {
        S3Configuration s3Config = useDefaultBucketObjectArea ? defaultS3Configuration : otherS3Configuration;
        S3Properties.S3Bucket s3Bucket = s3Config.getBucket();
        S3Client s3Client = s3Config.getS3Client();

        try {
            var request = GetObjectRequest.builder()
                .bucket(s3Bucket.getBucketName())
                .key(getKeyName(s3Bucket, keyName)).build();

            return s3Client.getObjectAsBytes(request).asUtf8String();
        } catch (Exception e) {
            throw new ObjectStorageException("An error occurred while communicating with S3 storage", e);
        }

    }

    private String getKeyName(S3Properties.S3Bucket s3Bucket, String keyName) {
        return String.format("%s/%s", s3Bucket.getObjectPrefix(), keyName);
    }

}
