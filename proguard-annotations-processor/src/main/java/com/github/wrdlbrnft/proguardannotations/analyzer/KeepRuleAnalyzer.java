package com.github.wrdlbrnft.proguardannotations.analyzer;

import com.github.wrdlbrnft.proguardannotations.KeepClass;
import com.github.wrdlbrnft.proguardannotations.KeepField;
import com.github.wrdlbrnft.proguardannotations.KeepMethod;
import com.github.wrdlbrnft.proguardannotations.KeepSetting;
import com.github.wrdlbrnft.proguardannotations.includestatements.IncludeStatement;
import com.github.wrdlbrnft.proguardannotations.keeprules.KeepAllRule;
import com.github.wrdlbrnft.proguardannotations.keeprules.KeepRule;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Xaver on 09/04/16.
 */
public class KeepRuleAnalyzer {

    public List<KeepRule> analyze(RoundEnvironment roundEnv) {
        return roundEnv.getElementsAnnotatedWith(KeepClass.class).stream()
                .map(TypeElement.class::cast)
                .map(this::analyzeClass)
                .collect(Collectors.toList());
    }

    private KeepRule analyzeClass(TypeElement element) {
        final KeepClass keepClassAnnotation = element.getAnnotation(KeepClass.class);
        final List<KeepSetting> settings = Arrays.asList(keepClassAnnotation.value());
        if (settings.contains(KeepSetting.ALL)) {
            return new KeepAllRule(element);
        }

        return element.getEnclosedElements().stream()
                .filter(member -> member.getKind() == ElementKind.FIELD || member.getKind() == ElementKind.METHOD)
                .filter(member -> memberHasKeepAnnotation(member) || settings.stream()
                        .map(KeepSettingEvaluator::of)
                        .anyMatch(evaluator -> evaluator.shouldKeep(member)))
                .map(IncludeStatement::of)
                .collect(() -> new KeepRule.Builder(element), KeepRule.Builder::add, (a, b) -> {
                    throw new IllegalStateException();
                })
                .build();
    }

    private boolean memberHasKeepAnnotation(Element member) {
        return member.getAnnotation(KeepMethod.class) != null
                || member.getAnnotation(KeepField.class) != null;
    }
}
