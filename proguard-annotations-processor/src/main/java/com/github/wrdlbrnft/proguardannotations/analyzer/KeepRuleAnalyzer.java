package com.github.wrdlbrnft.proguardannotations.analyzer;

import com.github.wrdlbrnft.proguardannotations.*;
import com.github.wrdlbrnft.proguardannotations.includestatements.IncludeStatement;
import com.github.wrdlbrnft.proguardannotations.keeprules.KeepAllRule;
import com.github.wrdlbrnft.proguardannotations.keeprules.KeepRule;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Xaver on 09/04/16.
 */
public class KeepRuleAnalyzer {

    private final Set<TypeElement> mHandledClasses = new HashSet<>();

    public Stream<KeepRule> analyze(RoundEnvironment roundEnv) {
        final List<KeepRule> rulesForKeptClasses = roundEnv.getElementsAnnotatedWith(KeepClass.class).stream()
                .map(TypeElement.class::cast)
                .peek(mHandledClasses::add)
                .map(this::transformKeptClass)
                .collect(Collectors.toList());

        final Stream<TypeElement> classesWithKeptMembers = Stream.concat(roundEnv.getElementsAnnotatedWith(KeepField.class).stream(), roundEnv.getElementsAnnotatedWith(KeepMethod.class).stream())
                .filter(element -> element.getAnnotation(DontKeep.class) == null)
                .filter(member -> !mHandledClasses.stream()
                        .filter(member.getEnclosingElement()::equals)
                        .findAny().isPresent())
                .map(Element::getEnclosingElement)
                .map(TypeElement.class::cast)
                .distinct();

        final Stream<TypeElement> classesWithKeptName = roundEnv.getElementsAnnotatedWith(KeepName.class).stream()
                .map(TypeElement.class::cast);

        return Stream.concat(
                rulesForKeptClasses.stream(),
                Stream.concat(classesWithKeptMembers, classesWithKeptName)
                        .distinct()
                        .map(this::transformClassWithKeptMembers)
        );
    }

    private KeepRule transformKeptClass(TypeElement element) {
        final KeepClass keepClassAnnotation = element.getAnnotation(KeepClass.class);
        final List<KeepSetting> settings = Arrays.asList(keepClassAnnotation.value());
        if (settings.contains(KeepSetting.ALL)) {
            return new KeepAllRule(element);
        }

        return element.getEnclosedElements().stream()
                .filter(member -> member.getKind() == ElementKind.FIELD || member.getKind() == ElementKind.METHOD)
                .filter(member -> member.getAnnotation(DontKeep.class) == null)
                .filter(member -> memberHasKeepAnnotation(member) || settings.stream()
                        .map(KeepSettingEvaluator::of)
                        .anyMatch(evaluator -> evaluator.shouldKeep(member)))
                .map(IncludeStatement::of)
                .collect(() -> new KeepRule.Builder(KeepRule.Type.KEEP_ALL, element), KeepRule.Builder::add, (a, b) -> {
                    throw new IllegalStateException("Failed to generate keep rules for " + element);
                })
                .build();
    }

    private KeepRule transformClassWithKeptMembers(TypeElement element) {
        final KeepRule.Type ruleType = element.getAnnotation(KeepName.class) != null
                ? KeepRule.Type.KEEP_ALL
                : KeepRule.Type.KEEP_MEMBERS;

        return element.getEnclosedElements().stream()
                .filter(this::memberHasKeepAnnotation)
                .map(IncludeStatement::of)
                .collect(() -> new KeepRule.Builder(ruleType, element), KeepRule.Builder::add, (a, b) -> {
                    throw new IllegalStateException("Failed to generate keep rules for " + element);
                })
                .build();
    }

    private boolean memberHasKeepAnnotation(Element member) {
        return member.getAnnotation(KeepMethod.class) != null
                || member.getAnnotation(KeepField.class) != null;
    }
}
