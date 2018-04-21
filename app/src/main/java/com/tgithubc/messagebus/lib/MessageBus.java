package com.tgithubc.messagebus.lib;

import android.os.Looper;
import android.util.Log;
import android.util.Pair;

import com.tgithubc.messagebus.IObserver;
import com.tgithubc.messagebus.lib.handler.ThreadMessageHandler;
import com.tgithubc.messagebus.lib.message.Message;
import com.tgithubc.messagebus.lib.message.RunThread;
import com.tgithubc.messagebus.lib.proxy.DefaultProxyHandler;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Bus
 * Created by tc :)
 */
public class MessageBus {

    private static final String TAG = "MessageBus";

    private ThreadMessageHandler mMainHandler, mWorkHandler;
    // 观察者集合
    private List<WeakReference<? extends IObserver>> mObservers;
    // 每一个事件接口，对应一个handler处理对象，循环处理所有相关订阅者的方法调用
    private Map<Class<? extends IObserver>, DefaultProxyHandler> mProxyMap;

    private MessageBus() {
        mObservers = new CopyOnWriteArrayList<>();
        mProxyMap = new ConcurrentHashMap<>();
        mWorkHandler = new ThreadMessageHandler();
        mMainHandler = new ThreadMessageHandler(Looper.getMainLooper());
    }

    private static class SingletonHolder {
        private static final MessageBus BUS = new MessageBus();
    }

    public static MessageBus instance() {
        return SingletonHolder.BUS;
    }

    /**
     * 获取观察者代理
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T extends IObserver> T get(Class<T> clazz) {
        DefaultProxyHandler handler = mProxyMap.get(clazz);
        if (handler == null) {
            handler = new DefaultProxyHandler(mObservers, clazz);
            mProxyMap.put(clazz, handler);
        }
        return (T) handler.get();
    }

    /**
     * 分发消息
     *
     * @param message
     */
    public void dispatch(Message message) {
        @RunThread
        int runThread = message.decorateInfo.runThread;
        long delayedTime = message.decorateInfo.delayedTime;

        switch (runThread) {
            case RunThread.MAIN:
                if (delayedTime > 0) {
                    mMainHandler.postDelayed(message, delayedTime);
                } else {
                    if (isMainThread()) {
                        message.run();
                    } else {
                        mMainHandler.post(message);
                    }
                }
                break;
            case RunThread.BACKGROUND:
                if (delayedTime > 0) {
                    mWorkHandler.postDelayed(message, delayedTime);
                } else {
                    if (!isMainThread()) {
                        message.run();
                    } else {
                        mWorkHandler.post(message);
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * 注册
     *
     * @param observer
     */
    public void register(IObserver observer) {
        if (observer == null || hasRegistered(observer)) {
            Log.w(TAG, "observer == null or has been registered");
            return;
        }
        mObservers.add(new WeakReference<>(observer));
        Log.d(TAG, "add size :" + mObservers.size() + ",mObservers :" + mObservers);
    }

    /**
     * 解注册
     *
     * @param observer
     */
    public void unRegister(IObserver observer) {
        if (observer == null) {
            return;
        }
        for (WeakReference<? extends IObserver> ref : mObservers) {
            IObserver ob = ref.get();
            if (observer.equals(ob) || ob == null) {
                mObservers.remove(ref);
            }
        }

        Iterator iterator = mProxyMap.keySet().iterator();
        while (iterator.hasNext()) {
            Class type = (Class) iterator.next();
            if (type.isInstance(observer)
                    && mProxyMap.get(type).getSameTypeObserverCount() == 0) {
                iterator.remove();
            }
        }

        if (mObservers.size() == 0) {
            mMainHandler.stop();
            mWorkHandler.stop();
        }
        Log.d(TAG, "remove size :" + mObservers.size() + ",mObservers :" + mObservers);
    }

    /**
     * 是否注册
     *
     * @param observer
     * @return
     */
    private boolean hasRegistered(IObserver observer) {
        for (WeakReference ref : mObservers) {
            IObserver o = (IObserver) ref.get();
            if (observer.equals(o)) {
                return true;
            }
        }
        return false;
    }

    private boolean isMainThread() {
        return Thread.currentThread() == Looper.getMainLooper().getThread();
    }
}
