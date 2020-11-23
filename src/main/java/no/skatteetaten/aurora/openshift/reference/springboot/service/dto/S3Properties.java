package no.skatteetaten.aurora.openshift.reference.springboot.service.dto;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "s3")
public class S3Properties {

    private Map<String, S3Bucket> buckets;

    public Map<String, S3Bucket> getBuckets() {
        return buckets;
    }

    public void setBuckets(Map<String, S3Bucket> buckets) {
        this.buckets = buckets;
    }

    public static class S3Bucket {
        private String serviceEndpoint;
        private String accessKey;
        private String secretKey;
        private String bucketName;
        private String objectPrefix;
        private String bucketRegion;

        public String getServiceEndpoint() {
            return serviceEndpoint;
        }

        public void setServiceEndpoint(String serviceEndpoint) {
            this.serviceEndpoint = serviceEndpoint;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getBucketName() {
            return bucketName;
        }

        public void setBucketName(String bucketName) {
            this.bucketName = bucketName;
        }

        public String getObjectPrefix() {
            return objectPrefix;
        }

        public void setObjectPrefix(String objectPrefix) {
            this.objectPrefix = objectPrefix;
        }

        public String getBucketRegion() {
            return bucketRegion;
        }

        public void setBucketRegion(String bucketRegion) {
            this.bucketRegion = bucketRegion;
        }
    }
}
