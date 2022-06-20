package com.mc.spring.actual.combat.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.mc.spring.actual.combat.model.CreateTableEntity;
import com.mc.spring.actual.combat.model.TransactionTest;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;

/**
 * @author macheng
 * @date 2021/12/3 11:12
 */
public interface CreateTableMapper extends RootMapper<CreateTableEntity> {

    @Select("select * from three  final where id =1")
    List<CreateTableEntity> testFinal();
    @Select("show create table ${tableName};")
    HashMap<String,String> getCreateSql(String tableName);


    @DS("doris")
    @Select(" ${create};")
    Integer create(String create);
}
