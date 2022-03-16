package org.mytest.util;

/**
 * <p>捕获受检异常的Runnable</p>
 * <p>运行时抛出异常</p>
 */
@FunctionalInterface
public interface MyRunnable extends Runnable {
    @Override
    default void run() {
        try {
            this.myRun();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void myRun() throws Exception;
}
