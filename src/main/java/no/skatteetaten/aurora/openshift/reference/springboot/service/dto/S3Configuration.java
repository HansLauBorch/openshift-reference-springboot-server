package no.skatteetaten.aurora.openshift.reference.springboot.service.dto;

import software.amazon.awssdk.services.s3.S3Client;

public class S3Configuration {
    private S3Client s3Client;
    private S3Properties.S3Bucket bucket;

    public S3Configuration(
        S3Client s3Client,
        S3Properties.S3Bucket bucket
    ) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    public S3Properties.S3Bucket getBucket() {
        return bucket;
    }

    public void setBucket(S3Properties.S3Bucket bucket) {
        this.bucket = bucket;
    }

    public S3Client getS3Client() {
        return s3Client;
    }

    public void setS3Client(S3Client s3Client) {
        this.s3Client = s3Client;
    }

}
