package com.mc.spring.actual.combat;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author macheng
 * @date 2022/2/19 9:30
 */
public class TestJoin {



    public static void main(String[] args) throws InterruptedException, IOException {
        ArrayList<String> s=new ArrayList<>();
        for (long i = 0; i < 10000; i++) {
            JSONObject json= new JSONObject();
            json.put("id",i);
            s.add(json.toJSONString());
            Thread.sleep(1);
        }

        for (long i = 0; i < 10000; i++) {
            JSONObject json= new JSONObject();
            json.put("ids",i);
            s.add(json.toJSONString());
            Thread.sleep(1);
        }

        System.in.read();
    }
}
