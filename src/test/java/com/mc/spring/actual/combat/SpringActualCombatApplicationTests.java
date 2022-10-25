package com.mc.spring.actual.combat;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mc.spring.actual.combat.mapper.TableNineMapper;
import com.mc.spring.actual.combat.mapper.TestThreeMapper;
import com.mc.spring.actual.combat.model.TableNineEntity;
import com.mc.spring.actual.combat.model.TestThree;
import com.mc.spring.actual.combat.service.CreateDorisService;
import com.mc.spring.actual.combat.service.TransactionTestService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.rebalance.AllocateMessageQueueAveragely;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
@Slf4j
class SpringActualCombatApplicationTests {

    @Autowired
    TransactionTestService transactionTestMapper;
    @Autowired
    private TestThreeMapper testThreeMapper;
    @Autowired
    private TableNineMapper tableNineMapper;
    @Autowired
    CreateDorisService createDorisService;

    @Test
    void contextLoads() {

//        IntStream.range(0, 20000).parallel().forEach(x -> {
//            ArrayList<TableNineEntity> tableNineEntities = new ArrayList<>();
//
//            for (int i = 0; i < 50; i++) {
//                tableNineEntities.add(new TableNineEntity().setCKey("" + x).setId(((long) x) * 50 + i)
//                        .setCValue("" + i).setCreateTime(new Date()).setUpdateTime(new Date()));
//            }
//
//            tableNineMapper.mysqlInsertOrUpdateBatch(tableNineEntities);
//            System.out.println("SUCCESS" + x);
//        });
        createDorisService.createDorisTableByMysql("hjy", "tenant", false);
    }

    @Test
    void testMQ() throws MQClientException, IOException {
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
        defaultMQPushConsumer.setPullBatchSize(1000);
        defaultMQPushConsumer.setNamesrvAddr("59.110.235.142:9876");
        defaultMQPushConsumer.setConsumerGroup("test");
        defaultMQPushConsumer.subscribe("CK_SYNC", "*");
        defaultMQPushConsumer.setConsumeMessageBatchMaxSize(1000);
        defaultMQPushConsumer.setAllocateMessageQueueStrategy(new AllocateMessageQueueAveragely());
        defaultMQPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        defaultMQPushConsumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                System.out.println("==================================================");
                System.out.println(list.size());
//               AtomicLong l=new AtomicLong(0);
//                list.forEach(x->{
//                   l.addAndGet(x.getBody().length);});
//                System.out.println(l.longValue()/1024);
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        defaultMQPushConsumer.start();
        System.in.read();
    }

    @Test
    void speedTest() {
        long simpleTotal = 0;
        long streamTotal = 0;
        for (int j = 0; j < 10; j++) {
            AtomicInteger simple = new AtomicInteger(0);
            AtomicInteger stream = new AtomicInteger(0);
            long start = System.currentTimeMillis();
            for (int i = 0; i < 2000; i++) {
                Page<TableNineEntity> all = tableNineMapper.selectPage(new Page<>(i * 500, 500, 1000000), new QueryWrapper<TableNineEntity>()
                        .between("id", i * 500 + 1, i * 500 + 500));
                simple.addAndGet(all.getRecords().size());
            }
            long simpleEnd = System.currentTimeMillis();
            tableNineMapper.selectStream(new QueryWrapper<TableNineEntity>().between("id", 1, 1000000), x -> {
                long value = x.getResultObject().getId() * 500;
                stream.incrementAndGet();
            });

            long streamEnd = System.currentTimeMillis();
            simpleTotal += simpleEnd - start;
            streamTotal += streamEnd - simpleEnd;
            System.out.println("simple cost: " + (simpleEnd - start) + " ms " + " , nums" + simple.get());
            System.out.println("stream cost: " + (streamEnd - simpleEnd) + " ms " + " , nums" + stream.get());
            System.out.println("==========================================");
        }

        System.out.println(" total :");
        System.out.println("simple: " + simpleTotal);
        System.out.println("stream: " + streamTotal);

    }

    @Test
    public void testDorisTransaction() {
        transactionTestMapper.testDorisTransaction();
    }


    @Test
    public void testFinal() {
        AtomicInteger i = new AtomicInteger(0);
        testThreeMapper.selectStream(new QueryWrapper<TestThree>().select("id"), new ResultHandler<TestThree>() {
            @Override
            public void handleResult(ResultContext<? extends TestThree> resultContext) {
                i.incrementAndGet();
            }
        });
        System.out.println(i);
    }

    @Test
    public void testDoris() {
        List<TestThree> id = testThreeMapper.selectList(new QueryWrapper<TestThree>().eq("id", 1));

        System.out.println(id.get(0).getCreateTime());
        int insert = testThreeMapper.insert(new TestThree().setId(2L).setCreateTime(LocalDateTime.now()));
    }
}
