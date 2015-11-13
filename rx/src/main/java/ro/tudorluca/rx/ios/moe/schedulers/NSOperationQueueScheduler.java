package ro.tudorluca.rx.ios.moe.schedulers;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ios.foundation.NSOperationQueue;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action0;
import rx.internal.util.RxThreadFactory;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;

/**
 * Created by tudor on 12/11/15.
 */
public class NSOperationQueueScheduler extends Scheduler {

    private final NSOperationQueue queue;

    public static NSOperationQueueScheduler from(NSOperationQueue queue) {
        if (queue == null) throw new NullPointerException("queue == null");
        return new NSOperationQueueScheduler(queue);
    }

    NSOperationQueueScheduler(NSOperationQueue queue) {
        this.queue = queue;
    }

    @Override
    public Worker createWorker() {
        return new NSOperationQueueWorker(queue);
    }

    static class NSOperationQueueWorker extends Worker {

        private final NSOperationQueue operationQueue;
        private final CompositeSubscription compositeSubscription = new CompositeSubscription();

        NSOperationQueueWorker(NSOperationQueue operationQueue) {
            this.operationQueue = operationQueue;
        }

        @Override
        public Subscription schedule(Action0 action) {
            return schedule(action, 0, TimeUnit.MILLISECONDS);
        }

        @Override
        public Subscription schedule(Action0 action, long delayTime, TimeUnit unit) {
            if (compositeSubscription.isUnsubscribed()) {
                return Subscriptions.unsubscribed();
            }

            final NSOperationQueueScheduledAction scheduledAction = new NSOperationQueueScheduledAction(operationQueue, action, compositeSubscription);
            compositeSubscription.add(scheduledAction);

            final ScheduledExecutorService executorService = IOSScheduledExecutorPool.getInstance();

            Future<?> future;
            if (delayTime > 0) {
                future = executorService.schedule(scheduledAction, delayTime, unit);
            } else {
                future = executorService.submit(scheduledAction);
            }

            scheduledAction.add(Subscriptions.from(future));

            return scheduledAction;
        }

        @Override
        public void unsubscribe() {
            compositeSubscription.unsubscribe();
        }

        @Override
        public boolean isUnsubscribed() {
            return compositeSubscription.isUnsubscribed();
        }
    }

    private static final class IOSScheduledExecutorPool {

        private static final String THREAD_PREFIX = "RX-iOS-ScheduledExecutorPool-";
        private static final RxThreadFactory RX_THREAD_FACTORY = new RxThreadFactory(THREAD_PREFIX);

        private static IOSScheduledExecutorPool instance;

        private final ScheduledExecutorService scheduledExecutorService;

        private IOSScheduledExecutorPool() {
            scheduledExecutorService = Executors.newScheduledThreadPool(1, RX_THREAD_FACTORY);
        }

        static ScheduledExecutorService getInstance() {
            if (instance == null) {
                instance = new IOSScheduledExecutorPool();
            }

            return instance.scheduledExecutorService;
        }
    }
}
