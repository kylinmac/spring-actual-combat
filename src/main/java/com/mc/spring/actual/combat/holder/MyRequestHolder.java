package com.mc.spring.actual.combat.holder;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


/**
 * @author macheng
 * @date 2022/1/7 9:47
 */
@Component
public class MyRequestHolder extends RequestContextHolder {

    private static final InheritableThreadLocal<RequestAttributes> inheritableThreadPoolRequestAttributesHolder = new InheritableThreadLocal<>();

    public static void setRequestAttributes(@Nullable RequestAttributes attributes, boolean inheritable) {
        RequestContextHolder.setRequestAttributes(attributes, inheritable);
        if (inheritable) {
            inheritableThreadPoolRequestAttributesHolder.set(attributes);
        }

    }

    public static void resetRequestAttributes() {
        RequestContextHolder.resetRequestAttributes();
        inheritableThreadPoolRequestAttributesHolder.remove();
    }

    public static RequestAttributes getRequestAttributes() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return inheritableThreadPoolRequestAttributesHolder.get();
        } else {
            return requestAttributes;
        }
    }

    public static void setRequestAttributes(@Nullable RequestAttributes attributes) {
        setRequestAttributes(attributes, true);
    }

}

