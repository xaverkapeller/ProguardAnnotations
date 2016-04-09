package com.github.wrdlbrnft.proguardannotations.keeprules;

import com.github.wrdlbrnft.proguardannotations.Utils;

import javax.lang.model.element.TypeElement;

/**
 * Created by Xaver on 09/04/16.
 */
public class KeepAllRule implements KeepRule {

    private final String mProguardRule;

    public KeepAllRule(TypeElement element) {
        mProguardRule = "-keep " + Utils.formatType(element) + " { *; }";
    }

    @Override
    public String toString() {
        return mProguardRule;
    }
}
