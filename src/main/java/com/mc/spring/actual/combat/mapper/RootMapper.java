package com.mc.spring.actual.combat.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.session.ResultHandler;

import java.util.List;

/**
 * @author macheng
 * @date 2021/5/19 13:39
 */
public interface RootMapper<T> extends BaseMapper<T> {

    /**
     * 自定义批量新增或更新
     * 如果要自动填充，@Param(xx) xx参数名必须是 list/collection/array 3个的其中之一
     */
    int mysqlInsertOrUpdateBatch(@Param("list") List<T> list);

    void updateByPrimaryKeySelective(T t);

    /**
     * @param wrapper
     * @param handler
     */
    @Options(resultSetType = ResultSetType.FORWARD_ONLY, fetchSize = Integer.MIN_VALUE)
    void selectStream(@Param(Constants.WRAPPER) Wrapper<T> wrapper, ResultHandler<T> handler);

}
