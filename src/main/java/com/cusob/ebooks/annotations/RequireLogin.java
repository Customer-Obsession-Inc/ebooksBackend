package com.cusob.ebooks.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE}) // 这个注解可以应用于方法和类
@Retention(RetentionPolicy.RUNTIME) // 注解在运行时可用
    public @interface RequireLogin {
}
