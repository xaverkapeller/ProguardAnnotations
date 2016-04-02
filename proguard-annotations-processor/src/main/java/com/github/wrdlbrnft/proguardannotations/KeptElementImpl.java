package com.github.wrdlbrnft.proguardannotations;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.TypeElement;

/**
 * Created by kapeller on 08/03/16.
 */
class KeptElementImpl implements KeptElement {

    public static KeptElement from(TypeElement element) {
        final KeepRules keepRulesAnnotation = element.getAnnotation(KeepRules.class);
        final Keep keepAnnotation = element.getAnnotation(Keep.class);
        final List<Keep> rules = new ArrayList<>();
        if (keepAnnotation != null) {
            rules.add(keepAnnotation);
        }
        if (keepRulesAnnotation != null) {
            for (Keep rule : keepRulesAnnotation.value()) {
                if (rule != null) {
                    rules.add(rule);
                }
            }
        }

        return new KeptElementImpl(
                element,
                rules
        );
    }

    private final TypeElement mTypeElement;
    private final List<Keep> mRules;

    public KeptElementImpl(TypeElement typeElement, List<Keep> rules) {
        this.mTypeElement = typeElement;
        this.mRules = rules;
    }

    @Override
    public TypeElement getTypeElement() {
        return mTypeElement;
    }

    @Override
    public List<Keep> getRules() {
        return mRules;
    }
}
