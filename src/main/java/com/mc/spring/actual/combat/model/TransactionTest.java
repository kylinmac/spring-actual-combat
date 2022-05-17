package com.mc.spring.actual.combat.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * @author macheng
 * @date 2022/1/12 19:19
 */
@TableName("transaction_test")
@Data
@Accessors(chain = true)
public class TransactionTest {
    private Long id;
    private BigDecimal deci;
    private String str;
    private Integer num;

}
