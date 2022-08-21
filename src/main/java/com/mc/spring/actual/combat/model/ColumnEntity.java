package com.mc.spring.actual.combat.model;

import lombok.Data;

/**
 * @author macheng
 * @date 2022/6/20 12:15
 */
@Data
public class ColumnEntity {
    private String tableName;
    private String columnName;
    private String isNullable;
    private String columnType;
    private String dataType;
    private String columnDefault;
    private String columnComment;
}
