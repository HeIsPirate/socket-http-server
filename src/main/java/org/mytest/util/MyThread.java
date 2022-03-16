package org.mytest.util;

/**
 * 配合使用{@link MyRunnable}
 */
public class MyThread extends Thread {
    public MyThread(MyRunnable runnable) {
        super(runnable);
    }
}
