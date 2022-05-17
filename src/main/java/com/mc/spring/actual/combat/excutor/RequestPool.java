package com.mc.spring.actual.combat.excutor;

import com.mc.spring.actual.combat.holder.MyRequestHolder;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Map;
import java.util.concurrent.*;

/**
 * @author macheng
 * @date 2022/1/7 10:17
 */
public class RequestPool extends ThreadPoolExecutor {

    public RequestPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public RequestPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public RequestPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public RequestPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @Override
    public synchronized void execute(Runnable command) {
        MyRequestHolder.setRequestAttributes(RequestContextHolder.getRequestAttributes());
        TaskWithThreadLocal task = new TaskWithThreadLocal(command, InheritableThreadLocal.getThreadLocalsMap());
        super.execute(task);
    }

    private static class TaskWithThreadLocal implements Runnable {
        private Map<Object, Object> threadLocalMap;
        private Runnable delegate;

        public TaskWithThreadLocal(Runnable delegate, Map<Object, Object> threadLocalMap) {
            this.delegate = delegate;
            this.threadLocalMap = threadLocalMap;
        }

        /**
         * 重写run方法，在执行run方法前设置线程变量，执行run方法后清除线程变量
         * 同时，打印了运行时的异常信息，并吞掉了delegate.run()运行时的异常，不往外抛
         * （线程池默认会在任务运行异常后抛出异常，并销毁掉线程对象本身，也就是如果每个任务都运行异常了，那么用线程池的效率还不如直接新建线程，详情见ThreadPoolExecutor类1123行runWorkers方法 ）
         * jdk线程池这样处理的意义应该是希望通过将异常抛出，将异常交给线程对象本身自带的异常处理拦截器或JVM默认的全局异常处理拦截器捕获并处理，
         * 这里直接去调用拦截器处理，不往外抛异常，避免线程实例的销毁
         */
        @Override
        public void run() {
            InheritableThreadLocal.setThreadLocalsFromMap(threadLocalMap);
            try {
                try {
                    delegate.run();
                    //由于callable的call()方法执行过程的异常会被它的调用上级FutureTask的run()方法中处理而使异常不往外抛，为了打印异常日志这里统一进行异常日志打印的处理
                    if (delegate instanceof FutureTask) {
                        try {
                            ((FutureTask) delegate).get();
                        } catch (Throwable e) {
                            Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                        }
                    }
                } catch (Throwable e) {
                    Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                }
            } finally {
                InheritableThreadLocal.removeThreadLocals();
            }
        }
    }

}
