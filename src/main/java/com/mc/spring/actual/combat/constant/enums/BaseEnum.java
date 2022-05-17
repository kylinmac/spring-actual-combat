package com.mc.spring.actual.combat.constant.enums;

/**
 * @author macheng
 * @date 2021/12/3 11:57
 */
public interface BaseEnum {
    /**
     * 获取枚举值
     *
     * @return
     */
    Integer getValue();

    /**
     * 获取枚举文本
     *
     * @return
     */
    String getLabel();

    /**根据枚举值和type获取枚举*/
    public static <T extends BaseEnum> T getEnum(Class<T> type, int value) {
        T[] objs = type.getEnumConstants();
        for (T em : objs) {
            if (em.getValue().equals(value)) {
                return em;
            }
        }
        return null;
    }
}
