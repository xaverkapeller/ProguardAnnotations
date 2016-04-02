package com.github.wrdlbrnft.proguardannotations;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;

/**
 * Created by kapeller on 07/03/16.
 */
public class ProguardAnnotationsProcessor extends AbstractProcessor {

    private static final Logger LOGGER = Logger.getLogger(ProguardAnnotationsProcessor.class.getSimpleName());

    private int round = 0;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        final Set<Element> elements = new HashSet<>();
        elements.addAll(roundEnv.getElementsAnnotatedWith(Keep.class));
        elements.addAll(roundEnv.getElementsAnnotatedWith(KeepRules.class));

        try {
            try (final Writer writer = openOutputFile()) {
                writer.append(elements.stream()
                        .filter(TypeElement.class::isInstance)
                        .map(TypeElement.class::cast)
                        .map(KeptElementImpl::from)
                        .map(KeepRuleFactory::transformToKeepRule)
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
        types.add(Keep.class.getCanonicalName());
        types.add(KeepRules.class.getCanonicalName());
        return types;
    }
}
