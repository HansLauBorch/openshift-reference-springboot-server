package no.skatteetaten.aurora.openshift.reference.springboot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShutdownHook extends Thread {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private static boolean hookCalled = false;

    public static boolean isHookCalled() {
        return hookCalled;
    }

    @Override
    public void run() {
        hookCalled = true;
        final int until = 10;
        log.info("Shutdown hook called, starting to loop "+until+" seconds");
        for ( int i=0 ; i < until; i++ ) {
            try {
                sleep(1000);
                log.info("... sleeping before shutdown... Count down: "+(10 - i));
            } catch (InterruptedException e) {
                log.error("Got: "+e);
                Thread.currentThread().interrupt();
            }
        }
    }
}
