package com.evilbeast.meizi.rx;


import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Author: sumary
 */
public class RxBus {
    private static volatile RxBus mInstance;
    private final Subject bus;

    public RxBus() {
        bus = new SerializedSubject<>(PublishSubject.create());
    }

    /**
     * 单例模式RxBus2
     */
    public static RxBus getInstance() {
        RxBus rxBus = mInstance;
        if (mInstance == null) {
            synchronized (RxBus.class){
                rxBus= mInstance;
                if (mInstance == null) {
                    rxBus = new RxBus();
                    mInstance = rxBus;
                }
            }
        }
        return rxBus;
    }

    /**
     * 发送消息
     */
    public void post(Object object) {
        bus.onNext(object);
    }

    /**
     * 接收消息
     */
    public <T> Observable<T> toObserverable(Class<T> eventType) {
        
        // Filters the items emitted by an Observable, only emitting those of the specified type.
        return bus.ofType(eventType);
    }
}

