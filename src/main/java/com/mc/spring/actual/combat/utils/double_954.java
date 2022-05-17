package com.mc.spring.actual.combat.utils;

import java.util.HashMap;

/**
 * @author macheng
 * @date 2022/4/1 12:17
 */
public class double_954 {
    public static boolean canReorderDoubled(int[] arr) {
        HashMap<Integer,Integer> map=new HashMap<>();
        int all=0;
        for (int i = 0; i < arr.length; i++) {
            System.out.println(map);
            Integer key=null;
            if(arr[i]%2==0){
                if(map.get(arr[i]/2)!=null){
                    key=arr[i]/2;
                }
                if(map.get(arr[i]*2)!=null){
                    key=arr[i]*2;
                }
            }else{
                if(map.get(arr[i]*2)!=null){
                    key=arr[i]*2;
                }

            }

            if (key!=null&&map.get(key)!=0){
                map.put(key,map.get(key)-1);
                System.out.println(key+","+arr[i]);
                all++;
                continue;
            }
            map.putIfAbsent(arr[i],0);
            map.put(arr[i],map.get(arr[i])+1);
        }
        System.out.println(all);
        return all>=arr.length/2;
    }

    public static void main(String[] args) {
        boolean b = canReorderDoubled(new int[]{2,4,0,0,8,1});
        System.out.println(b);
    }
}
