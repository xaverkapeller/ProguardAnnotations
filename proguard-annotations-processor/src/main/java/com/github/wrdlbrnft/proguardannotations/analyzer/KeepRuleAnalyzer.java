package com.github.wrdlbrnft.proguardannotations.analyzer;

import com.github.wrdlbrnft.proguardannotations.*;
import com.github.wrdlbrnft.proguardannotations.includestatements.IncludeStatement;
import com.github.wrdlbrnft.proguardannotations.keeprules.KeepRule;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Xaver on 09/04/16.
 */
public class KeepRuleAnalyzer {

    private static final KeepSetting[] INNER_CLASS_SETTINGS = new KeepSetting[]{
            KeepSetting.PUBLIC_INNER_CLASSES, KeepSetting.PROTECTED_INNER_CLASSES,
            KeepSetting.PACKAGE_LOCAL_INNER_CLASSES, KeepSetting.PRIVATE_INNER_CLASSES
    };

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

        final KeepRule.Type keepRuleType = element.getAnnotation(KeepClass.class) != null
                ? KeepRule.Type.KEEP_ALL
                : KeepRule.Type.KEEP_MEMBERS;

        return element.getEnclosedElements().stream()
                .filter(Utils.not(this::hasDontKeepAnnotation))
                .filter(member -> hasKeepAnnotation(member) || settings.stream()
                        .map(KeepSettingEvaluator::of)
                        .anyMatch(evaluator -> evaluator.shouldKeep(member)))
                .map(IncludeStatement::of)
                .collect(() -> new KeepRule.Builder(keepRuleType, element), KeepRule.Builder::add, (a, b) -> {
                    throw new IllegalStateException("Failed to generate keep rules for " + element);
                })
                .build();
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
                .map(TypeElement.class::cast);
    }

    private Collection<KeepSettingEvaluator> getEvaluatorsForInnerClasses(Collection<KeepSetting> settings) {
        final Set<KeepSettingEvaluator> evaluators = new HashSet<>();
        for (KeepSetting setting : INNER_CLASS_SETTINGS) {
            if (settings.contains(setting)) {
                evaluators.add(KeepSettingEvaluator.of(setting));
            }
        }
        return evaluators;
    }

    private boolean hasDontKeepAnnotation(Element member) {
        return member.getAnnotation(DontKeep.class) != null;
    }

    private boolean hasKeepAnnotation(Element member) {
        return member.getAnnotation(KeepMember.class) != null;
    }
}
