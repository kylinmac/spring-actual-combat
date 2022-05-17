package com.mc.spring.actual.combat.excutor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 可在特定线程池中继承的线程变量（配合InheritableThreadLocalExecutor使用）
 * Created by cd_huang on 2017/8/28.
 */
public class InheritableThreadLocal<T> extends ThreadLocal<T>{

    private static List<InheritableThreadLocal> inheritableExecutorThreadLocalList =new CopyOnWriteArrayList<>();

    public InheritableThreadLocal(){
        this(true);
    }

    public InheritableThreadLocal(boolean isAdd){
        /**
         * 一般线程变量本身也不需要被垃圾回收
         */
        if(isAdd){
            inheritableExecutorThreadLocalList.add(this);
        }
    }

    /**
     * 从map里取出内容set线程变量（protected方法，可重写，但不提倡直接调用）
     * @param map
     */
    protected void setThreadLocalFromMap(Map map){
        T obj = (T)map.get(this);
        this.set(obj);
    }

    /**
     * get线程变量装到map里（protected方法，可重写，但不提倡直接调用）
     * @param map
     */
    protected void getThreadLocalputMap(Map map){
        T obj = this.get();
        map.put(this,obj);
    }

    /**
     * 移除掉线程变量（protected方法，可重写，但不提倡直接调用）
     */
    protected void removeThreadLocal(){
        this.remove();
    }

    /**
     * 把当前线程可传递的线程变量内容放在map里，在task放进线程池队列前调用
     * @return
     */
    public static Map<Object,Object> getThreadLocalsMap(){
        Map<Object,Object> threadLocalMap =new HashMap<>();
        List<InheritableThreadLocal> list =inheritableExecutorThreadLocalList;
        for(InheritableThreadLocal threadLocal:list){
            threadLocal.getThreadLocalputMap(threadLocalMap);
        }
        return threadLocalMap;
    }

    /**
     * 把map里的内容重新set线程变量内容，在task真正运行run方法前调用
     * @param threadLocalMap
     */
    public static void setThreadLocalsFromMap(Map<Object,Object> threadLocalMap){
        List<InheritableThreadLocal> list =inheritableExecutorThreadLocalList;
        for(InheritableThreadLocal threadLocal:list){
            threadLocal.setThreadLocalFromMap(threadLocalMap);
        }
    }

    /**
     * 把setThreadLocalsFromMap方法set的线程变量内容清空，在task真正运行run方法后调用
     */
    public static void removeThreadLocals(){
        List<InheritableThreadLocal> list =inheritableExecutorThreadLocalList;
        for(InheritableThreadLocal threadLocal:list){
            threadLocal.removeThreadLocal();
        }
    }
}