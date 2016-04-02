package com.github.wrdlbrnft.proguardannotations;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

/**
 * Created by kapeller on 30/03/16.
 */
public interface MemberEvaluator {
    boolean shouldKeep(Element element);

    static MemberEvaluator of(KeepSetting setting) {
        switch (setting) {

            case ALL:
                return element -> true;

            case NAME:
                return element -> false;

            case PUBLIC_MEMBERS:
                return element -> element.getModifiers().contains(Modifier.PUBLIC);

            case PROTECTED_MEMBERS:
                return element -> element.getModifiers().contains(Modifier.PROTECTED);

            case PACKAGE_LOCAL_MEMBERS:
                return element -> element.getModifiers().contains(Modifier.DEFAULT);

            case PRIVATE_MEMBERS:
                return element -> element.getModifiers().contains(Modifier.PRIVATE);

            case PUBLIC_METHODS:
                return element -> element.getKind() == ElementKind.METHOD && element.getModifiers().contains(Modifier.PUBLIC);

            case PROTECTED_METHODS:
                return element -> element.getKind() == ElementKind.METHOD && element.getModifiers().contains(Modifier.PROTECTED);

            case PACKAGE_LOCAL_METHODS:
                return element -> element.getKind() == ElementKind.METHOD && element.getModifiers().contains(Modifier.DEFAULT);

            case PRIVATE_METHODS:
                return element -> element.getKind() == ElementKind.METHOD && element.getModifiers().contains(Modifier.PRIVATE);

            case PUBLIC_FIELDS:
                return element -> element.getKind() == ElementKind.FIELD && element.getModifiers().contains(Modifier.PUBLIC);

            case PROTECTED_FIELDS:
                return element -> element.getKind() == ElementKind.FIELD && element.getModifiers().contains(Modifier.PROTECTED);

            case PACKAGE_LOCAL_FIELDS:
                return element -> element.getKind() == ElementKind.FIELD && element.getModifiers().contains(Modifier.DEFAULT);

            case PRIVATE_FIELDS:
                return element -> element.getKind() == ElementKind.FIELD && element.getModifiers().contains(Modifier.PRIVATE);

            default:
                throw new IllegalStateException("Encountered unknown KeepSetting: " + setting);
        }
    }
}
