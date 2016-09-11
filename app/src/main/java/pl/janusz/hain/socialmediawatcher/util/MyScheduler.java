package pl.janusz.hain.socialmediawatcher.util;

import java.util.concurrent.Executors;

import rx.Scheduler;
import rx.schedulers.Schedulers;

public final class MyScheduler {

    private static Scheduler scheduler = null;

    private MyScheduler() {
    }

    public static synchronized Scheduler getScheduler() {
        if(scheduler == null){
            scheduler = Schedulers.from(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
        }
        return scheduler;
    }
}
