package com.github.wrdlbrnft.proguardannotations.includestatements;

import com.github.wrdlbrnft.proguardannotations.Utils;

import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Created by Xaver on 09/04/16.
 */
class IncludeStatementImpl implements IncludeStatement {

    private final Element mElement;

    IncludeStatementImpl(Element element) {
        mElement = element;
    }

    @Override
    public String toProguardKeepStatement(ProcessingEnvironment processingEnv) {
        return formatMemberModifiers(mElement) + " "
                + formatMemberType(processingEnv, mElement) + " "
                + formatMemberName(mElement) + ";";
    }

    private static String formatMemberName(Element member) {
        if (member instanceof ExecutableElement) {
            final ExecutableElement method = (ExecutableElement) member;
            return formatMethodSignature(method);
        }

        return member.getSimpleName().toString();
    }

    private static String formatMethodSignature(ExecutableElement method) {
        return method.getSimpleName() + method.getParameters().stream()
                .map(VariableElement::asType)
                .map(TypeMirror::toString)
                .collect(Collectors.joining(", ", "(", ")"));
    }

    private static String formatMemberType(ProcessingEnvironment processingEnv, Element member) {
        if (member instanceof ExecutableElement) {
            final ExecutableElement method = (ExecutableElement) member;
            return Utils.getProguardClassName(processingEnv, method.getReturnType());
        }

        return Utils.getProguardClassName(processingEnv, member.asType());
    }

    private static String formatMemberModifiers(Element element) {
        return element.getModifiers().stream()
                .map(Modifier::name)
                .map(String::toLowerCase)
                .collect(Collectors.joining(" "));
    }
}
