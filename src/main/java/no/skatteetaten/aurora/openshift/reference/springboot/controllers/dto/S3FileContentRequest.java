package no.skatteetaten.aurora.openshift.reference.springboot.controllers.dto;

public class S3FileContentRequest {

    private String fileName;
    private String content;

    private boolean useDefaultBucketObjectArea;

    public S3FileContentRequest(String fileName, String content, boolean useDefaultBucketObjectArea) {
        this.fileName = fileName;
        this.content = content;
        this.useDefaultBucketObjectArea = useDefaultBucketObjectArea;
    }

    public S3FileContentRequest() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isUseDefaultBucketObjectArea() {
        return useDefaultBucketObjectArea;
    }

    public void setUseDefaultBucketObjectArea(boolean useDefaultBucketObjectArea) {
        this.useDefaultBucketObjectArea = useDefaultBucketObjectArea;
    }
}
