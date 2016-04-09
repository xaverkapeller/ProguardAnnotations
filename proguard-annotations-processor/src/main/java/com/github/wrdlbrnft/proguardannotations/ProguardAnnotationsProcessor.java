package com.github.wrdlbrnft.proguardannotations;

import com.github.wrdlbrnft.proguardannotations.keeprules.KeepRule;
import com.github.wrdlbrnft.proguardannotations.analyzer.KeepRuleAnalyzer;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;

/**
 * Created by kapeller on 07/03/16.
 */
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ProguardAnnotationsProcessor extends AbstractProcessor {

    private static final Logger LOGGER = Logger.getLogger(ProguardAnnotationsProcessor.class.getSimpleName());

    private final KeepRuleAnalyzer mKeepRuleAnalyzer = new KeepRuleAnalyzer();

    private int round = 0;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final List<KeepRule> rules = mKeepRuleAnalyzer.analyze(roundEnv);
        try {
            try (final Writer writer = openOutputFile()) {
                writer.append(rules.stream()
                        .map(KeepRule::toString)
                        .collect(Collectors.joining("\n"))
                );
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to analyze your code.");
            LOGGER.log(Level.SEVERE, "Failed to analyze code.", e);
        }

        return false;
    }

    private Writer openOutputFile() throws IOException {
        return processingEnv.getFiler()
                .createResource(StandardLocation.SOURCE_OUTPUT, "com.github.wrdlbrnft", String.format("generated_proguard_part_%d.pro", round++))
                .openWriter();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        final Set<String> types = new HashSet<>();
        types.add(KeepClass.class.getCanonicalName());
        return types;
    }
}
