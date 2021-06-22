package no.skatteetaten.aurora.openshift.reference.springboot.service;

import org.springframework.stereotype.Service;

@Service
public class ExampleService {

    private static final int SECOND = 1000;

    public boolean performOperationThatMayFail() {

        long sleepTime = (long) (Math.random() * SECOND);

        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Sleep interupted", e);
        }

        return sleepTime % 2 == 0;
    }

}
