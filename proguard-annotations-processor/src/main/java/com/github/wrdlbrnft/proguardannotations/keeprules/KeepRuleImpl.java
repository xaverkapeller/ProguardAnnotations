package com.github.wrdlbrnft.proguardannotations.keeprules;

import com.github.wrdlbrnft.proguardannotations.Utils;
import com.github.wrdlbrnft.proguardannotations.includestatements.IncludeStatement;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Xaver on 09/04/16.
 */
class KeepRuleImpl implements KeepRule {

    private final Type mType;
    private final TypeElement mElement;
    private final List<IncludeStatement> mIncludeStatement;

    KeepRuleImpl(Type type, TypeElement element, List<IncludeStatement> includeStatement) {
        mType = type;
        mElement = element;
        mIncludeStatement = includeStatement;
    }

    @Override
    public String toProguardKeepRule(ProcessingEnvironment processingEnv) {
        return mIncludeStatement.stream()
                .map(statement -> statement.toProguardKeepStatement(processingEnv))
                .collect(Collectors.joining(
                        "\n\t",
                        getKeepInstructionForType(mType) + " " + formatType(processingEnv, mElement) + " {\n\t",
                        "\n}"
                ));
    }

    private static String getKeepInstructionForType(Type type) {
        switch (type) {

            case KEEP_NAME_AND_MEMBERS:
                return "-keep";

            case KEEP_MEMBERS:
                return "-keepclassmembers";

            default:
                throw new IllegalStateException("Encountered unhandled KeepRule.Type: " + type);
        }
    }

    private static String formatType(ProcessingEnvironment processingEnv, TypeElement element) {
        return formatElementModifiers(element)
                + " " + getNameOfKind(element)
                + " " + Utils.getProguardClassName(element);
    }

    private static String formatElementModifiers(Element element) {
        final Set<Modifier> blacklist = new HashSet<>();
        blacklist.add(Modifier.STATIC);

        return element.getModifiers().stream()
                .filter(Utils.not(blacklist::contains))
                .map(Modifier::name)
                .map(String::toLowerCase)
                .collect(Collectors.joining(" "));
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
