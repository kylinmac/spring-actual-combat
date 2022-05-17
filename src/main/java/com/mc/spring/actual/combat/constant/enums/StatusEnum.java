package com.mc.spring.actual.combat.constant.enums;

/**
 * @author macheng
 * @date 2021/12/3 13:34
 */
public enum StatusEnum implements BaseEnum{
    /**
     * 待处理
     */
    PENDING(5, "待处理"),
    /**
     * 进行中
     */
    DOING(10, "进行中"),
    /**
     * 已完成
     */
    COMPLETED(15, "已完成");

    private final Integer value;
    private final String label;

    StatusEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public String getLabel() {
        return label;
    }

}
