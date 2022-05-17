package com.mc.spring.actual.combat;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author macheng
 * @date 2022/2/19 9:30
 */
public class TestJoin {



    public static void main(String[] args) throws InterruptedException {
         ReentrantLock getLock=new ReentrantLock();


        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                getLock.lock();
            }
        });
        thread.start();
        Thread.sleep(1000);
        thread.join();
        System.out.println("end");
    }
}
