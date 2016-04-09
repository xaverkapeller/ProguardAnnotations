package com.github.wrdlbrnft.proguardannotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by kapeller on 07/03/16.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface KeepClass {
    KeepSetting[] value() default KeepSetting.ALL;
}
