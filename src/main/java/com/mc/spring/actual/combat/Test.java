package com.mc.spring.actual.combat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author macheng
 * @date 2021/12/7 16:08
 */
public class Test {

    public static int countValidWords(String sentence) {
        String[] s = sentence.split(" ");
        System.out.println(Arrays.toString(s));
        int result = 0;
        A:
        for (String s1 : s) {
            s1 = s1.trim();
            if (s1.length() == 0) {
                continue;
            }
            int heng = 0;
            B:
            for (int i = 0; i < s1.length(); i++) {
                if (s1.charAt(i) <= '9' && s1.charAt(i) >= '0') {
                    continue A;
                } else if (s1.charAt(i) == '-') {
                    if (i == 0 || i == s1.length() - 1 || heng != 0 || (i < s1.length() - 1 && !(s1.charAt(i + 1) <= 'z' && s1.charAt(i + 1) >= 'a'))) {
                        continue A;
                    }
                    heng++;
                } else if (s1.charAt(i) <= 'z' && s1.charAt(i) >= 'a') {
                    continue B;
                } else {
                    if (i != s1.length() - 1) {
                        continue A;
                    }
                }
            }
            result++;
        }
        return result;
    }

    public static int bestRotation(int[] nums) {

        int[] dp = new int[nums.length];

        for (int i = 0; i < nums.length; i++) {

            int num = nums[i];
            if (num > nums.length - 1) {

            } else {
                for (int j = num; j <= nums.length - 1; j++) {
                    if (i >= j) {
                        dp[i - j]++;
                    } else {
                        dp[i + (nums.length - j)]++;
                    }
                }
            }
        }
        int max = 0;
        int k = 0;
        for (int i = 0; i < dp.length; i++) {
            if (dp[i] > max) {
                max = dp[i];
                k = i;
            }
        }
        return k;
    }

    public static void main(String[] args) throws Exception {

//        bestRotation(new int[]{2, 3, 1, 4, 0});

        System.out.println(1&2);
    }

    public void produce() {
        //        int i = countValidWords(" 62   nvtk0wr4f  8 qt3r! w1ph 1l ,e0d 0n 2v 7c.  n06huu2n9 s9   ui4 nsr!d7olr  q-, vqdo!btpmtmui.bb83lf g .!v9-lg 2fyoykex uy5a 8v whvu8 .y sc5 -0n4 zo pfgju 5u 4 3x,3!wl  fv4   s  aig cf j1 a i  8m5o1  !u n!.1tz87d3 .9    n a3  .xb1p9f  b1i a j8s2 cugf l494cx1! hisceovf3 8d93 sg 4r.f1z9w   4- cb r97jo hln3s h2 o .  8dx08as7l!mcmc isa49afk i1 fk,s e !1 ln rt2vhu 4ks4zq c w  o- 6  5!.n8ten0 6mk 2k2y3e335,yj  h p3 5 -0  5g1c  tr49, ,qp9 -v p  7p4v110926wwr h x wklq u zo 16. !8  u63n0c l3 yckifu 1cgz t.i   lh w xa l,jt   hpi ng-gvtk8 9 j u9qfcd!2  kyu42v dmv.cst6i5fo rxhw4wvp2 1 okc8!  z aribcam0  cp-zp,!e x  agj-gb3 !om3934 k vnuo056h g7 t-6j! 8w8fncebuj-lq    inzqhw v39,  f e 9. 50 , ru3r  mbuab  6  wz dw79.av2xp . gbmy gc s6pi pra4fo9fwq k   j-ppy -3vpf   o k4hy3 -!..5s ,2 k5 j p38dtd   !i   b!fgj,nx qgif ");
//        System.out.println(i);

//        // 实例化消息生产者Producer
//        DefaultMQProducer producer = new DefaultMQProducer("please_rename_unique_group_name");
//        // 设置NameServer的地址
//        producer.setNamesrvAddr("localhost:9876");
//        // 启动Producer实例
//        producer.start();
//        for (int i = 0; i < 1; i++) {
//            // 创建消息，并指定Topic，Tag和消息体
//            Message msg = new Message("bill-multi" /* Topic */,
//                    "TagA" /* Tag */,
//                    ("Hello RocketMQ " + i).getBytes(RemotingHelper.DEFAULT_CHARSET) /* Message body */
//            );
//            // 发送消息到一个Broker
//            SendResult sendResult = producer.send(msg);
//            // 通过sendResult返回消息是否成功送达
//            System.out.printf("%s%n", sendResult);
//        }
//        // 如果不再发送消息，关闭Producer实例。
//        producer.shutdown();
    }

    public int secondMinimum(int n, int[][] edges, int time, int change) {
        HashMap<Integer, List<Integer>> map = new HashMap<>();

        for (int i = 0; i < edges.length; i++) {
            map.putIfAbsent(edges[i][0], new ArrayList<>());
            map.get(edges[i][0]).add(edges[i][1]);

            map.putIfAbsent(edges[i][1], new ArrayList<>());
            map.get(edges[i][1]).add(edges[i][0]);
        }


        return 0;
    }

    public int numberOfWeakCharacters(int[][] properties) {
        Arrays.sort(properties, (o1, o2) -> o1[0] - o2[0]);
        int cur = properties[0][1];
        int result = 0;
        for (int i = 1; i < properties.length; i++) {
            if (properties[i][1] < cur) {
                result++;
            }
            cur = Math.max(cur, properties[i][1]);
        }
        return 0;
    }


}
