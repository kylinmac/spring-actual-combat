//package com.mc.spring.actual.combat.datasync.parse;
//
//import com.alibaba.otter.canal.client.CanalConnector;
//import com.alibaba.otter.canal.client.rocketmq.RocketMQCanalConnector;
//import com.alibaba.otter.canal.common.CanalException;
//import com.alibaba.otter.canal.protocol.CanalEntry;
//import com.alibaba.otter.canal.protocol.Message;
//import com.google.protobuf.ByteString;
//import com.google.protobuf.InvalidProtocolBufferException;
//import com.mc.spring.actual.combat.datasync.entry.EntryMeta;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * @author macheng
// * @date 2022/1/26 12:14
// */
//public class CanalEntryMetaParse extends EntryMetaParse<EntryMeta, Message>{
//
//    private static int batchSize = 8192;
//    private String mode;
//    private  static String namesrv;
//    private static  String topic;
//    private static String group;
//    private static boolean debug = false;
//
//    private RocketMQCanalConnector connector;
//    @Override
//    EntryMeta parse(Message message) {
//        List<CanalEntry.Entry> entries;
//        if (message.isRaw()) {
//            entries = new ArrayList<>(message.getRawEntries().size());
//            for (ByteString rawEntry : message.getRawEntries()) {
//                try {
//                    entries.add(CanalEntry.Entry.parseFrom(rawEntry));
//                } catch (InvalidProtocolBufferException e) {
//                    throw new CanalException(e);
//                }
//            }
//        } else {
//            entries = message.getEntries();
//        }
//
//        for (CanalEntry.Entry entry : entries) {
//            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN
//                    || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
//                continue;
//            }
//
//            CanalEntry.RowChange rowChange;
//            try {
//                rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
//            } catch (Exception e) {
//                throw new RuntimeException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(),
//                        e);
//            }
//        }
//        return null;
//    }
//
//    public static void main(String[] args) {
//        RocketMQCanalConnector connector =  new RocketMQCanalConnector(namesrv, topic, group, batchSize, false);
//        connector.connect();
//        while (true){
//            Message withoutAck = connector.getWithoutAck(batchSize);
//        }
//    }
//}
