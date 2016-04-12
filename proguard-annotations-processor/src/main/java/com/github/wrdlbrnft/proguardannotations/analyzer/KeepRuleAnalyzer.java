package com.github.wrdlbrnft.proguardannotations.analyzer;

import com.github.wrdlbrnft.proguardannotations.*;
import com.github.wrdlbrnft.proguardannotations.includestatements.IncludeStatement;
import com.github.wrdlbrnft.proguardannotations.keeprules.KeepRule;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Xaver on 09/04/16.
 */
public class KeepRuleAnalyzer {

    private static final Collection<KeepSetting> INNER_CLASS_SETTINGS = Collections.unmodifiableCollection(Arrays.asList(
            KeepSetting.ALL,
            KeepSetting.PUBLIC_INNER_CLASSES, KeepSetting.PROTECTED_INNER_CLASSES,
            KeepSetting.PACKAGE_LOCAL_INNER_CLASSES, KeepSetting.PRIVATE_INNER_CLASSES
    ));

    public Stream<KeepRule> analyze(RoundEnvironment roundEnv) {
        final Stream<TypeElement> keptTypes = roundEnv.getElementsAnnotatedWith(KeepClass.class).stream()
                .map(TypeElement.class::cast);

        final Stream<TypeElement> keptMemberTypes = roundEnv.getElementsAnnotatedWith(KeepMember.class).stream()
                .filter(Utils.not(this::hasDontKeepAnnotation))
                .map(Element::getEnclosingElement)
                .filter(TypeElement.class::isInstance)
                .map(TypeElement.class::cast)
                .distinct();

        final Stream<TypeElement> keptClassMembersTypes = roundEnv.getElementsAnnotatedWith(KeepClassMembers.class).stream()
                .map(TypeElement.class::cast);

        final List<TypeElement> types = Stream.concat(Stream.concat(keptTypes, keptMemberTypes), keptClassMembersTypes)
                .distinct()
                .collect(Collectors.toList());

        final Stream<TypeElement> keptInnerClasses = types.stream()
                .flatMap(this::findKeptInnerClasses);

        return Stream.concat(types.stream(), keptInnerClasses)
                .distinct()
                .map(this::transformToKeepRule);
    }

    private KeepRule transformToKeepRule(TypeElement element) {
        final KeepClassMembers kepClassMembersAnnotation = element.getAnnotation(KeepClassMembers.class);
        final Collection<KeepSetting> settings = kepClassMembersAnnotation != null
                ? Arrays.asList(kepClassMembersAnnotation.value())
                : Collections.emptyList();

        final KeepRule.Type keepRuleType = determineKeepRuleType(element);

        return element.getEnclosedElements().stream()
                .filter(Utils.not(this::hasDontKeepAnnotation))
                .filter(member -> member.getKind() != ElementKind.CLASS)
                .filter(member -> member.getKind() != ElementKind.INTERFACE)
                .filter(member -> member.getKind() != ElementKind.ANNOTATION_TYPE)
                .filter(member -> member.getKind() != ElementKind.ENUM)
                .filter(member -> hasKeepAnnotation(member) || settings.stream()
                        .map(KeepSettingEvaluator::of)
                        .anyMatch(evaluator -> evaluator.shouldKeep(member)))
                .map(IncludeStatement::of)
                .collect(() -> new KeepRule.Builder(keepRuleType, element), KeepRule.Builder::add, (a, b) -> {
                    throw new IllegalStateException("Failed to generate keep rules for " + element);
                })
                .build();
    }

    private KeepRule.Type determineKeepRuleType(TypeElement element) {
        final KeepClass keepClassAnnotation = element.getAnnotation(KeepClass.class);
        if(keepClassAnnotation != null) {
            return KeepRule.Type.KEEP_ALL;
        }

        if(element.getEnclosingElement() instanceof TypeElement) {
            final TypeElement enclosingType = (TypeElement) element.getEnclosingElement();
            final KeepClassMembers annotation = enclosingType.getAnnotation(KeepClassMembers.class);
            if(annotation == null) {
                return KeepRule.Type.KEEP_ALL;
            }
            
            final Collection<KeepSetting> settings = Arrays.asList(annotation.value());
            if(settings.contains(KeepSetting.ALL)) {
                return KeepRule.Type.KEEP_ALL;
            }

            final Set<Modifier> modifiers = element.getModifiers();
            if(modifiers.contains(Modifier.PUBLIC) && settings.contains(KeepSetting.PUBLIC_INNER_CLASSES)
                    || modifiers.contains(Modifier.PROTECTED) && settings.contains(KeepSetting.PROTECTED_INNER_CLASSES)
                    || modifiers.contains(Modifier.DEFAULT) && settings.contains(KeepSetting.PACKAGE_LOCAL_INNER_CLASSES)
                    || modifiers.contains(Modifier.PRIVATE) && settings.contains(KeepSetting.PRIVATE_INNER_CLASSES)) {
                return KeepRule.Type.KEEP_ALL;
            }
        }

        return KeepRule.Type.KEEP_MEMBERS;
    }

    private Stream<TypeElement> findKeptInnerClasses(TypeElement type) {
        final KeepClassMembers annotation = type.getAnnotation(KeepClassMembers.class);
        if (annotation == null) {
            return Stream.empty();
        }

        final Collection<KeepSetting> settings = Arrays.asList(annotation.value());
        final Collection<KeepSettingEvaluator> evaluators = getEvaluatorsForInnerClasses(settings);
        return type.getEnclosedElements().stream()
                .filter(member -> member.getKind() == ElementKind.CLASS
                        || member.getKind() == ElementKind.ENUM
                        || member.getKind() == ElementKind.INTERFACE
                        || member.getKind() == ElementKind.ANNOTATION_TYPE)
                .filter(member -> evaluators.stream().anyMatch(evaluator -> evaluator.shouldKeep(member)))
                .map(TypeElement.class::cast)
                .flatMap(member -> Stream.concat(Stream.of(member), findKeptInnerClasses(member)));
    }

    private Collection<KeepSettingEvaluator> getEvaluatorsForInnerClasses(Collection<KeepSetting> settings) {
        return settings.stream()
                .filter(INNER_CLASS_SETTINGS::contains)
                .map(KeepSettingEvaluator::of)
                .collect(Collectors.toSet());
    }

    private boolean hasDontKeepAnnotation(Element member) {
        return member.getAnnotation(DontKeep.class) != null;
    }

    private boolean hasKeepAnnotation(Element member) {
        return member.getAnnotation(KeepMember.class) != null;
    }
}
