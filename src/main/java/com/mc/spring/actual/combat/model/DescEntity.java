package com.mc.spring.actual.combat.model;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 * @author macheng
 * @date 2022/7/5 19:22
 */
@Data
public class DescEntity {
    private String field;
    private String type;
    @TableField("Null")
    private String nullable;
    private String key;
    @TableField("Default")
    private String defaultValue;

}
