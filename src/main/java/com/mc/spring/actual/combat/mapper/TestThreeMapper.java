package com.mc.spring.actual.combat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mc.spring.actual.combat.model.TestThree;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author macheng
 * @date 2021/12/3 11:12
 */
public interface TestThreeMapper extends RootMapper<TestThree> {

    @Select("select * from three  final where id =1")
    List<TestThree> testFinal();
}
