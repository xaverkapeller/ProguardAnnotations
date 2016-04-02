package com.github.wrdlbrnft.proguardannotations;

import javax.lang.model.element.TypeElement;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by kapeller on 08/03/16.
 */
public interface KeptElement {
    TypeElement getTypeElement();
    List<Keep> getRules();
}
