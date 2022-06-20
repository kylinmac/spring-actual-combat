package com.mc.spring.actual.combat.model;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author macheng
 * @date 2021/12/3 11:09
 */
@TableName("cdc_mysql_to_doris")
@Data
@Accessors(chain = true)
public class CreateTableEntity {
    private String tableName;
    private String dorisTableName;
    private String keyColumns;
    private String distributedColumns;
    private String bitmapColumns;
    private String bloomfilterColumns;
    private String model;
    private String patitionColumns;
//    @TableField(typeHandler = EnumTypeHandler.class)
//    private StatusEnum c;
}
