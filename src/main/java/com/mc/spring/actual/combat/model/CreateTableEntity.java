package com.mc.spring.actual.combat.model;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
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
@Builder
public class CreateTableEntity {
    private Long id;
    private String mysqlTableName;
    private String mysqlSchemaName;
    private String dorisTableName;
    private String keyColumns;
    private String distributedColumns;
    private String bitmapColumns;
    private String bloomFilterColumns;
    private String tableModel;
    private String partitionColumns;
    private String partitionRange;
    private String partitionType;
    private String operator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
