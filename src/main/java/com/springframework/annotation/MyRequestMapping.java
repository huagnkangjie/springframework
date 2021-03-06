package com.springframework.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 处理请求地址
 * @author huang_kangjie
 * @date 2018-12-28 20:13
 * @since 1.0.3
 **/
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestMapping {

     //目前为了简单的测试，只做了单个url可以扩展为数组
     String value() default "";

}
