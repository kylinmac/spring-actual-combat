package com.mc.spring.actual.combat.controller;

import com.mc.spring.actual.combat.excutor.RequestPool;
import com.mc.spring.actual.combat.holder.MyRequestHolder;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.*;

/**
 * @author macheng
 * @date 2022/1/7 9:45
 */
@RestController
@RequestMapping("/request")
@Log4j2
public class RequestController {

    RequestPool executor=new RequestPool(2,2,1000, TimeUnit.SECONDS,new ArrayBlockingQueue<>(100));

    @GetMapping("/get")
    public String get(){
        return "1";
    }

    @RequestMapping("/get1")
    public String getRequest() throws ExecutionException, InterruptedException {
        RequestAttributes requestAttributes = MyRequestHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        String value = request.getHeader("value");
        log.info("===============current thread value:{}",value);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                RequestAttributes requestAttributes = MyRequestHolder.getRequestAttributes();
                try {
                    Thread.sleep(2000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("runnable sleep end===============");
                if (requestAttributes!=null){
                    HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
                    String value = request.getHeader("value");
                    log.info("===============thread pool execute value:{}",value);
                }

            }
        });

        executor.submit(new Runnable() {
            @Override
            public void run() {
                RequestAttributes requestAttributes = MyRequestHolder.getRequestAttributes();
                if (requestAttributes!=null){
                    HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
                    String value = request.getHeader("value");
                    log.info("===============thread pool submit value:{}",value);
                }

            }
        }).get();

        FutureTask futureTask = new FutureTask<>(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                RequestAttributes requestAttributes = MyRequestHolder.getRequestAttributes();
                if (requestAttributes!=null){
                    HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
                    String value = request.getHeader("value");
                    log.info("===============future value:{}",value);
                }
                return null;
            }
        });
        futureTask.run();
        futureTask.get();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("runnable sleep end===============");
                    String value = request.getHeader("value");
                    log.info("===============runnable value:{}",value);
            }
        }).start();

        return value;
    }
}
