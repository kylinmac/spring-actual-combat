package com.mc.spring.actual.combat.mybatis;

import java.util.*;

/**
 * @author macheng
 * @date 2022/3/30 12:27
 */
public class Busy {
    public List<Integer> busiestServers(int k, int[] arrival, int[] load) {
        int[] nums=new int[k];
        PriorityQueue<int[]> priorityQueue=new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        TreeSet<Integer> av=new TreeSet<>();
        for (int i = 0; i < k; i++) {
            av.add(k);
        }
        for(int i=0;i<arrival.length;i++){
          while (!priorityQueue.isEmpty()&&priorityQueue.peek()[1]<=arrival[i]){
              av.add(priorityQueue.poll()[0]);
          }
          if (av.isEmpty()){
              continue;
          }
          Integer ceiling = av.ceiling(i % k);
          if (ceiling==null){
              ceiling=av.first();
          }
          priorityQueue.add(new int[]{ceiling,arrival[i]+load[i]});
          nums[ceiling]++;

        }

        ArrayList<Integer> result=new ArrayList<>();
        int max=nums[0];
        for(int i=1;i<k;i++){
            max=Math.max(max,nums[i]);
        }

        for(int i=0;i<k;i++){
            if(nums[i]==max){
                result.add(i);
            }
        }
        return result;
    }
}
