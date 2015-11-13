package ro.tudorluca.rx.ios.moe.schedulers;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import ios.foundation.NSBlockOperation;
import ios.foundation.NSOperationQueue;
import rx.Subscription;
import rx.exceptions.OnErrorNotImplementedException;
import rx.functions.Action0;
import rx.internal.util.SubscriptionList;
import rx.plugins.RxJavaPlugins;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tudor on 12/11/15.
 */
public class NSOperationQueueScheduledAction extends AtomicReference<Thread> implements Runnable, Subscription {
    /** */
    private static final long serialVersionUID = -3962399486978279857L;
    final SubscriptionList cancel;
    final Action0 action;
    final NSOperationQueue queue;
    final NSBlockOperation operation;

    public NSOperationQueueScheduledAction(NSOperationQueue queue, Action0 action) {
        this.queue = queue;
        this.action = action;
        this.cancel = new SubscriptionList();
        operation = NSBlockOperation.blockOperationWithBlock(this::executeAction);
    }

    public NSOperationQueueScheduledAction(NSOperationQueue queue, Action0 action, CompositeSubscription parent) {
        this.queue = queue;
        this.action = action;
        this.cancel = new SubscriptionList(new Remover(this, parent));
        operation = NSBlockOperation.blockOperationWithBlock(this::executeAction);
    }

    public NSOperationQueueScheduledAction(NSOperationQueue queue, Action0 action, SubscriptionList parent) {
        this.queue = queue;
        this.action = action;
        this.cancel = new SubscriptionList(new Remover2(this, parent));
        operation = NSBlockOperation.blockOperationWithBlock(this::executeAction);
    }

    @Override
    public void run() {
        queue.addOperation(operation);
    }

    private void executeAction() {
        try {
            lazySet(Thread.currentThread());
            action.call();
        } catch (Throwable e) {
            // nothing to do but print a System error as this is fatal and there is nowhere else to throw this
            IllegalStateException ie;
            if (e instanceof OnErrorNotImplementedException) {
                ie = new IllegalStateException("Exception thrown on Scheduler.Worker thread. Add `onError` handling.", e);
            } else {
                ie = new IllegalStateException("Fatal Exception thrown on Scheduler.Worker thread.", e);
            }
            RxJavaPlugins.getInstance().getErrorHandler().handleError(ie);
            Thread thread = Thread.currentThread();
            thread.getUncaughtExceptionHandler().uncaughtException(thread, ie);
        } finally {
            unsubscribe();
        }
    }

    @Override
    public boolean isUnsubscribed() {
        return cancel.isUnsubscribed();
    }

    @Override
    public void unsubscribe() {
        if (!cancel.isUnsubscribed()) {
            operation.cancel();
            cancel.unsubscribe();
        }
    }

    /**
     * Adds a general Subscription to this {@code ScheduledAction} that will be unsubscribed
     * if the underlying {@code action} completes or the this scheduled action is cancelled.
     *
     * @param s the Subscription to add
     */
    public void add(Subscription s) {
        cancel.add(s);
    }

    /**
     * Adds the given Future to the unsubscription composite in order to support
     * cancelling the underlying task in the executor framework.
     *
     * @param f the future to add
     */
    public void add(final Future<?> f) {
        cancel.add(new FutureCompleter(f));
    }

    /**
     * Adds a parent {@link CompositeSubscription} to this {@code ScheduledAction} so when the action is
     * cancelled or terminates, it can remove itself from this parent.
     *
     * @param parent the parent {@code CompositeSubscription} to add
     */
    public void addParent(CompositeSubscription parent) {
        cancel.add(new Remover(this, parent));
    }

    /**
     * Adds a parent {@link CompositeSubscription} to this {@code ScheduledAction} so when the action is
     * cancelled or terminates, it can remove itself from this parent.
     *
     * @param parent the parent {@code CompositeSubscription} to add
     */
    public void addParent(SubscriptionList parent) {
        cancel.add(new Remover2(this, parent));
    }

    /**
     * Cancels the captured future if the caller of the call method
     * is not the same as the runner of the outer ScheduledAction to
     * prevent unnecessary self-interrupting if the unsubscription
     * happens from the same thread.
     */
    private final class FutureCompleter implements Subscription {
        private final Future<?> f;

        private FutureCompleter(Future<?> f) {
            this.f = f;
        }

        @Override
        public void unsubscribe() {
            if (NSOperationQueueScheduledAction.this.get() != Thread.currentThread()) {
                f.cancel(true);
            } else {
                f.cancel(false);
            }
        }

        @Override
        public boolean isUnsubscribed() {
            return f.isCancelled();
        }
    }

    /**
     * Remove a child subscription from a composite when unsubscribing.
     */
    private static final class Remover extends AtomicBoolean implements Subscription {

        private static final long serialVersionUID = 247232374289553518L;
        final NSOperationQueueScheduledAction s;
        final CompositeSubscription parent;

        public Remover(NSOperationQueueScheduledAction s, CompositeSubscription parent) {
            this.s = s;
            this.parent = parent;
        }

        @Override
        public boolean isUnsubscribed() {
            return s.isUnsubscribed();
        }

        @Override
        public void unsubscribe() {
            if (compareAndSet(false, true)) {
                parent.remove(s);
            }
        }
    }

    /**
     * Remove a child subscription from a composite when unsubscribing.
     */
    private static final class Remover2 extends AtomicBoolean implements Subscription {

        private static final long serialVersionUID = 247232374289553518L;
        final NSOperationQueueScheduledAction s;
        final SubscriptionList parent;

        public Remover2(NSOperationQueueScheduledAction s, SubscriptionList parent) {
            this.s = s;
            this.parent = parent;
        }

        @Override
        public boolean isUnsubscribed() {
            return s.isUnsubscribed();
        }

        @Override
        public void unsubscribe() {
            if (compareAndSet(false, true)) {
                parent.remove(s);
            }
        }
    }
}
