package com.github.wrdlbrnft.proguardannotations;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by Xaver on 09/04/16.
 */
public class Utils {

    public static <T> Predicate<T> not(Predicate<T> predicate) {
        return predicate.negate();
    }

    public static String getProguardClassName(TypeElement element) {
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

    public static String getProguardClassName(ProcessingEnvironment processingEnv, TypeMirror mirror) {
        final TypeKind kind = mirror.getKind();

        System.out.println(mirror.toString() + " -> " + kind);

        if (kind == TypeKind.TYPEVAR) {
            return "**";
        }

        if (kind == TypeKind.WILDCARD) {
            return "**";
        }

        if (kind == TypeKind.INTERSECTION) {
            return "**";
        }

        if (kind == TypeKind.ARRAY) {
            final ArrayType arrayType = (ArrayType) mirror;
            return getProguardClassName(processingEnv, arrayType.getComponentType()) + "[]";
        }

        if (mirror instanceof DeclaredType) {
            final DeclaredType declaredType = (DeclaredType) mirror;

            final String resolveTypeParameters = declaredType.getTypeArguments().isEmpty()
                    ? ""
                    : declaredType.getTypeArguments().stream()
                    .map(argument -> getProguardClassName(processingEnv, argument))
                    .collect(Collectors.joining(", ", "<", ">"));

            final TypeMirror erasedType = processingEnv.getTypeUtils().erasure(declaredType);
            final Element element = processingEnv.getTypeUtils().asElement(erasedType);
            if (element instanceof TypeElement) {
                final TypeElement typeElement = (TypeElement) element;
                return getProguardClassName(typeElement) + resolveTypeParameters;
            }
            return erasedType.toString() + resolveTypeParameters;
        }

        final Element element = processingEnv.getTypeUtils().asElement(mirror);
        if (element instanceof TypeElement) {
            final TypeElement typeElement = (TypeElement) element;
            return getProguardClassName(typeElement);
        }
        return mirror.toString();
    }
}
