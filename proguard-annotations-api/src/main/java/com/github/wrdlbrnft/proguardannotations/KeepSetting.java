package com.github.wrdlbrnft.proguardannotations;

/**
 * Created by kapeller on 09/03/16.
 */
public enum KeepSetting {
    ALL,
    NAME,

    PUBLIC_MEMBERS,
    PROTECTED_MEMBERS,
    PACKAGE_LOCAL_MEMBERS,
    PRIVATE_MEMBERS,

    PUBLIC_METHODS,
    PROTECTED_METHODS,
    PACKAGE_LOCAL_METHODS,
    PRIVATE_METHODS,

    PUBLIC_FIELDS,
    PROTECTED_FIELDS,
    PACKAGE_LOCAL_FIELDS,
    PRIVATE_FIELDS
}
