package com.github.wrdlbrnft.proguardannotations;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Created by kapeller on 08/03/16.
 */
public class KeepRuleFactory {

    public static String transformToKeepRule(KeptElement element) {
        final TypeElement typeElement = element.getTypeElement();
        final Set<KeepSetting> settings = element.getRules().stream()
                .map(Keep::value)
                .collect(Collectors.toSet());

        return "-keep " + formatElementModifiers(typeElement) + " " + getNameOfKind(typeElement) + " " + getProguardClassName(typeElement) + " {"
                + processRules(typeElement, settings)
                + "}";
    }

    private static String processRules(TypeElement typeElement, Set<KeepSetting> settings) {
        if (settings.contains(KeepSetting.ALL)) {
            return " *; ";
        }

        final Set<MemberEvaluator> evaluators = settings.stream()
                .map(MemberEvaluator::of)
                .collect(Collectors.toSet());

        return typeElement.getEnclosedElements().stream()
                .filter(element -> element.getEnclosingElement() == typeElement)
                .filter(element -> element.getKind() == ElementKind.METHOD || element.getKind() == ElementKind.FIELD)
                .filter(element -> evaluators.stream()
                        .anyMatch(evaluator -> evaluator.shouldKeep(element)))
                .map(KeepRuleFactory::transformMemberForKeepRule)
                .collect(Collectors.joining("\n\t", "\n\t", "\n"));
    }

    private static String transformMemberForKeepRule(Element member) {
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

    private static <T> Predicate<T> not(Predicate<T> predicate) {
        return predicate.negate();
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
        return typeElement.getKind().name().toLowerCase();
    }
}
