package com.mc.spring.actual.combat.injector;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.mc.spring.actual.combat.mybatis.method.MysqlInsertOrUpdateBatchMethod;
import com.mc.spring.actual.combat.mybatis.method.StreamSelectMethod;
import com.mc.spring.actual.combat.mybatis.method.UpdateByPrimaryKeySelectiveMethod;

import java.util.List;

/**
 * @author macheng
 * @date 2021/5/19 13:42
 */
public class CustomizedSqlInjector extends DefaultSqlInjector {
    /**
     * 如果只需增加方法，保留mybatis plus自带方法，
     * 可以先获取super.getMethodList()，再添加add
     */
    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass, TableInfo tableInfo) {
        List<AbstractMethod> methodList = super.getMethodList(mapperClass,tableInfo);
        methodList.add(new MysqlInsertOrUpdateBatchMethod());
        methodList.add(new UpdateByPrimaryKeySelectiveMethod());
        methodList.add(new StreamSelectMethod());
        return methodList;
    }
}
