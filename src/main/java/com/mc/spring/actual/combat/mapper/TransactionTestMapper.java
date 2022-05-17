package com.mc.spring.actual.combat.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.mc.spring.actual.combat.model.TransactionTest;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author macheng
 * @date 2022/1/12 19:21
 */

public interface TransactionTestMapper extends RootMapper<TransactionTest> {
    @Select("begin;")
    void begin();
    @Select("commit;")
    void commit();
    @Select("rollback;")
    void rollback();
    @Select("delete from transaction_test  where id in ( select id from  transaction_test  where ${ew.sqlSegment} )")
    void testDelete(@Param(Constants.WRAPPER) Wrapper<TransactionTest> query);
}

