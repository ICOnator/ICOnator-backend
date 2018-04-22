package io.iconator.commons.test.utils;

import java.util.LinkedList;
import java.util.List;

public class ThreadTestUtils {

    public static void runMultiThread(Runnable runnable, int threadCount) throws InterruptedException {
        List<Thread> threadList = new LinkedList<Thread>();
        for (int i = 0; i < threadCount; i++) {
            threadList.add(new Thread(runnable));
        }
        for (Thread t : threadList) {
            t.start();
        }
        for (Thread t : threadList) {
            t.join();
        }
    }

}
