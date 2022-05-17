package com.mc.spring.actual.combat.model;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mybatis.handler.EnumTypeHandler;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author macheng
 * @date 2021/12/3 11:09
 */
@TableName("canal_test")
@Data
@Accessors(chain = true)
public class TestThree {
    @TableId
    private Long  id;
    private LocalDateTime createTime;
//    @TableField(typeHandler = EnumTypeHandler.class)
//    private StatusEnum c;
}
