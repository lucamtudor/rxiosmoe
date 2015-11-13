package com.tudorluca.sample.ui;

import com.intel.inde.moe.natj.general.Pointer;
import com.intel.inde.moe.natj.general.ann.Owned;
import com.intel.inde.moe.natj.general.ann.RegisterOnStartup;
import com.intel.inde.moe.natj.objc.ObjCRuntime;
import com.intel.inde.moe.natj.objc.ann.ObjCClassName;
import com.intel.inde.moe.natj.objc.ann.Property;
import com.intel.inde.moe.natj.objc.ann.Selector;

import java.util.concurrent.TimeUnit;

import ios.NSObject;
import ios.foundation.NSThread;
import ios.uikit.UIButton;
import ios.uikit.UILabel;
import ios.uikit.UIViewController;
import ro.tudorluca.rx.ios.moe.schedulers.IOSSchedulers;
import rx.Observable;
import rx.Subscriber;
import rx.exceptions.OnErrorThrowable;
import rx.schedulers.Schedulers;

/**
 * @noinspection JniMissingFunction
 */
@com.intel.inde.moe.natj.general.ann.Runtime(ObjCRuntime.class)
@ObjCClassName("AppViewController")
@RegisterOnStartup
public class AppViewController extends UIViewController {

    @Owned
    @Selector("alloc")
    public static native AppViewController alloc();

    @Selector("init")
    public native AppViewController init();

    protected AppViewController(Pointer peer) {
        super(peer);
    }

    public UILabel statusText = null;
    public UIButton helloButton = null;

    @Override
    public void viewDidLoad() {
        statusText = getLabel();
        helloButton = getHelloButton();

        statusText.setText("...");
    }

    @Selector("statusText")
    @Property
    public native UILabel getLabel();

    @Selector("helloButton")
    @Property
    public native UIButton getHelloButton();

    @Selector("BtnPressedCancel_helloButton:")
    public void BtnPressedCancel_button(NSObject sender) {
        sampleObservable()
                .subscribeOn(Schedulers.newThread())
                .observeOn(IOSSchedulers.mainThread())
                .doOnSubscribe(() -> statusText.setText("Loading..."))
                .doOnCompleted(() -> statusText.setText("Completed!"))
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        System.out.println("onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.println("onError");
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(String s) {
                        System.out.println("onNext " + Thread.currentThread().getName());
                        System.out.println("onNext NS" + NSThread.currentThread().name());
                        System.out.println("onNext(" + s + ")");

                        statusText.setText(s);
                    }
                });
    }

    private static Observable<String> sampleObservable() {
        return Observable.defer(() -> {
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(5));
            } catch (InterruptedException e) {
                throw OnErrorThrowable.from(e);
            }

            System.out.println("onCreate " + Thread.currentThread().getName());
            System.out.println("onCreate NS" + NSThread.currentThread().name());

            return Observable.interval(1, TimeUnit.SECONDS)
                    .take(10)
                    .map(String::valueOf);
        });
    }
}
