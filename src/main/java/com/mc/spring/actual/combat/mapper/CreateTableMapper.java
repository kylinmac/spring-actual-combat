package com.mc.spring.actual.combat.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.mc.spring.actual.combat.model.ColumnEntity;
import com.mc.spring.actual.combat.model.CreateTableEntity;
import com.mc.spring.actual.combat.model.DescEntity;
import com.mc.spring.actual.combat.model.DorisPartitionEntity;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;

/**
 * @author macheng
 * @date 2021/12/3 11:12
 */
public interface CreateTableMapper extends RootMapper<CreateTableEntity> {

    @Select("show create table ${tableName};")
    @DS("hjy")
    HashMap<String, String> getCreateSql(String tableName);

    @Select("show tables;")
    @DS("hjy")
    List<HashMap<String, String>> getTables();

    @Select("show partitions from ${tableName};")
    @DS("hjydw")
    List<DorisPartitionEntity> getDorisTablePartition(String tableName);

    @Select("desc ${tableName};")
    List<DescEntity> getDesc(String tableName);

    @Select("select TABLE_COMMENT from INFORMATION_SCHEMA.TABLES where TABLE_NAME = #{tableName} and TABLE_SCHEMA=#{schemaName};")
    String getTableComment(String tableName, String schemaName);

    @Select("select table_name,COLUMN_NAME,DATA_TYPE,IS_NULLABLE,COLUMN_TYPE,COLUMN_COMMENT from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME=#{tableName} and TABLE_SCHEMA=#{schemaName};")
    List<ColumnEntity> getColumnInfo(String tableName, String schemaName);

    @DS("hjy")
    @Select(" ${execute};")
    Integer execute(String execute);
    @DS("avatica")
    @Select(" ${execute};")
    List<HashMap<String, String>> testAvatica(String execute);
    @DS("hjy")
    @Select(" ${count};")
    Long count(String count);


    @Select("select * from cdc_mysql_to_doris where mysql_table_name=#{tableName} and mysql_schema_Name=#{schemaName} and table_Model='unique'")
    CreateTableEntity getUniqueConfig(String tableName, String schemaName);


}
