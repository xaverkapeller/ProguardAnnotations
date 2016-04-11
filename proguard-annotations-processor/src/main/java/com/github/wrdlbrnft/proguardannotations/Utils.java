package com.github.wrdlbrnft.proguardannotations;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by Xaver on 09/04/16.
 */
public class Utils {

    public static <T> Predicate<T> not(Predicate<T> predicate) {
        return predicate.negate();
    }

    public static String formatType(TypeElement element) {
        return formatElementModifiers(element)
                + " " + getNameOfKind(element)
                + " " + getProguardClassName(element);
    }

    public static String formatMember(Element member) {
        return formatMemberModifiers(member) + " " + formatMemberType(member) + " " + formatMemberName(member) + ";";
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

    private static String formatMemberType(Element member) {
        if (member instanceof ExecutableElement) {
            final ExecutableElement method = (ExecutableElement) member;
            return method.getReturnType().toString();
        }

        return member.asType().toString();
    }

    private static String formatMemberModifiers(Element element) {
        return element.getModifiers().stream()
                .map(Modifier::name)
                .map(String::toLowerCase)
                .collect(Collectors.joining(" "));
    }

    private static String formatElementModifiers(Element element) {
        final Set<Modifier> blacklist = new HashSet<>();
        blacklist.add(Modifier.STATIC);

        return element.getModifiers().stream()
                .filter(not(blacklist::contains))
                .map(Modifier::name)
                .map(String::toLowerCase)
                .collect(Collectors.joining(" "));
    }

    private static String getProguardClassName(TypeElement element) {
        Element enclosingElement = element.getEnclosingElement();
        String className = element.getSimpleName().toString();
        while (enclosingElement.getKind() != ElementKind.PACKAGE) {
            className = enclosingElement.getSimpleName() + "$" + className;
            enclosingElement = enclosingElement.getEnclosingElement();
        }
        final PackageElement packageElement = (PackageElement) enclosingElement;
        final String packageName = packageElement.getQualifiedName().toString();

        return packageName + "." + className;
    }

    private static String getNameOfKind(TypeElement typeElement) {
        final ElementKind kind = typeElement.getKind();
        switch (kind) {

            case PACKAGE:
                return "package";

            case ENUM:
                return "enum";

            case CLASS:
                return "class";

            case ANNOTATION_TYPE:
                return "@interface";

            case INTERFACE:
                return "interface";

            default:
                return kind.name().toLowerCase();
        }
    }
}
