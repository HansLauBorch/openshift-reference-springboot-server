package no.skatteetaten.aurora.openshift.reference.springboot.service.exceptions;

public class ObjectStorageException extends RuntimeException {
    public ObjectStorageException(String msg, Exception e) {
        super(msg, e);
    }
}
