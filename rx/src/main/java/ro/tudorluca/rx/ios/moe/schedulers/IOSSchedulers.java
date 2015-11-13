package ro.tudorluca.rx.ios.moe.schedulers;

import ios.foundation.NSOperationQueue;
import ro.tudorluca.rx.ios.moe.plugins.RxIOSPlugins;
import rx.Scheduler;

/**
 * Created by tudor on 13/11/15.
 */
public final class IOSSchedulers {

    private IOSSchedulers() {
        throw new AssertionError("No instances");
    }

    private static final Scheduler MAIN_THREAD_SCHEDULER =
            new NSOperationQueueScheduler(NSOperationQueue.mainQueue());

    public static Scheduler mainThread() {
        final Scheduler scheduler = RxIOSPlugins.getInstance().getSchedulersHook().getMainThreadScheduler();
        return scheduler != null ? scheduler : MAIN_THREAD_SCHEDULER;
    }
}
