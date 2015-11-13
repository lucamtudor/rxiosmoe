package ro.tudorluca.rx.ios.moe.plugins;

import ro.tudorluca.rx.ios.moe.schedulers.IOSSchedulers;
import rx.Scheduler;
import rx.functions.Action0;

/**
 * Created by tudor on 13/11/15.
 */
public class RxIOSSchedulersHook {

    private static final RxIOSSchedulersHook DEFAULT_INSTANCE = new RxIOSSchedulersHook();

    public static RxIOSSchedulersHook getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    /**
     * Scheduler to return from {@link IOSSchedulers#mainThread()} or {@code null} if default
     * should be used.
     * <p/>
     * This instance should be or behave like a stateless singleton.
     */
    public Scheduler getMainThreadScheduler() {
        return null;
    }

    /**
     * Invoked before the Action is handed over to the scheduler.  Can be used for
     * wrapping/decorating/logging. The default is just a passthrough.
     *
     * @param action action to schedule
     * @return wrapped action to schedule
     */
    public Action0 onSchedule(Action0 action) {
        return action;
    }
}
