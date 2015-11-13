package ro.tudorluca.rx.ios.moe.plugins;

/**
 * Created by tudor on 13/11/15.
 */

import java.util.concurrent.atomic.AtomicReference;

import rx.annotations.Beta;

/**
 * Registry for plugin implementations that allows global override and handles the retrieval of
 * correct implementation based on order of precedence:
 * <ol>
 * <li>plugin registered globally via {@code register} methods in this class</li>
 * <li>default implementation</li>
 * </ol>
 */
public final class RxIOSPlugins {
    private static final RxIOSPlugins INSTANCE = new RxIOSPlugins();

    public static RxIOSPlugins getInstance() {
        return INSTANCE;
    }

    private final AtomicReference<RxIOSSchedulersHook> schedulersHook =
            new AtomicReference<RxIOSSchedulersHook>();

    RxIOSPlugins() { }

    /**
     * Reset any explicit or default-set hooks.
     * <p>
     * Note: This should only be used for testing purposes.
     */
    @Beta
    public void reset() {
        schedulersHook.set(null);
    }

    /**
     * Retrieves the instance of {@link RxIOSSchedulersHook} to use based on order of
     * precedence as defined in the {@link RxIOSPlugins} class header.
     * <p>
     * Override the default by calling {@link #registerSchedulersHook(RxIOSSchedulersHook)} or by
     * setting the property {@code rxandroid.plugin.RxAndroidSchedulersHook.implementation} with the
     * full classname to load.
     */
    public RxIOSSchedulersHook getSchedulersHook() {
        if (schedulersHook.get() == null) {
            schedulersHook.compareAndSet(null, RxIOSSchedulersHook.getDefaultInstance());
            // We don't return from here but call get() again in case of thread-race so the winner will
            // always get returned.
        }
        return schedulersHook.get();
    }

    /**
     * Registers an {@link RxIOSPlugins} implementation as a global override of any
     * injected or default implementations.
     *
     * @throws IllegalStateException if called more than once or after the default was initialized
     *                               (if usage occurs before trying to register)
     */
    public void registerSchedulersHook(RxIOSSchedulersHook impl) {
        if (!schedulersHook.compareAndSet(null, impl)) {
            throw new IllegalStateException(
                    "Another strategy was already registered: " + schedulersHook.get());
        }
    }
}
