package no.skatteetaten.aurora.openshift.reference.springboot.controllers.dto;

public class S3FileContentResponse {
    private String content;

    public S3FileContentResponse(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
