package com.github.wrdlbrnft.proguardannotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Xaver on 11/04/16.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface KeepClassMembers {
    KeepSetting[] value() default KeepSetting.ALL;
}
