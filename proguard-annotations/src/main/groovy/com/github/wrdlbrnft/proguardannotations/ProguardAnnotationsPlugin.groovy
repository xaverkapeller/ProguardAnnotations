package com.github.wrdlbrnft.proguardannotations

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException

/**
 * Created by Xaver on 02/04/16.
 */
class ProguardAnnotationsPlugin implements Plugin<Project> {

    private static final String GENERATED_RULE_FILE_NAME = 'generated-proguard-rules.pro'

    @Override
    void apply(Project project) {
        def variants = determineVariants(project)
        ensureAptPluginIsApplied(project)

        project.afterEvaluate {
            project.android[variants].all { variant ->
                configureVariant(project, variant)
            }

            project.android.buildTypes.each {
                it.proguardFiles new File(project.buildDir, GENERATED_RULE_FILE_NAME)
            }
        }

        project.dependencies {
            provided 'com.github.wrdlbrnft:proguard-annotations-api:0.2.0.24'
            apt 'com.github.wrdlbrnft:proguard-annotations-processor:0.2.0.24'
        }
    }

    private static String determineVariants(Project project) {
        if (project.plugins.findPlugin("com.android.application")) {
            return "applicationVariants";
        } else if (project.plugins.findPlugin("com.android.library")) {
            return "libraryVariants";
        } else {
            throw new ProjectConfigurationException("The com.android.application or com.android.library plugin must be applied to the project", null)
        }
    }

    private static void ensureAptPluginIsApplied(Project project) {
        if (!project.plugins.findPlugin('com.neenbedankt.android-apt')) {
            throw new ProjectConfigurationException('Android APT Plugin is required for ProguardAnnotations to work. ' +
                    'You need to apply it before applying proguard-annotations', null)
        }
    }

    private static Set<File> findProguardFiles(File folder) {
        def proguardFiles = new HashSet();
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                proguardFiles.addAll(findProguardFiles(file))
                continue
            }

            def fileName = file.name
            if (fileName.startsWith('generated_proguard_part_') && fileName.endsWith('.pro')) {
                proguardFiles.add(file)
            }
        }
        return proguardFiles;
    }

    private static void configureVariant(Project project, def variant) {
        def rootOutputDir = project.file(new File(project.buildDir, "generated/source/apt"))
        def variantOutputDir = new File(rootOutputDir, variant.dirName)
        def javaCompile = variant.hasProperty('javaCompiler') ? variant.javaCompiler : variant.javaCompile
        javaCompile.doLast {
            def generatedProguardFile = new File(project.buildDir, GENERATED_RULE_FILE_NAME)
            generatedProguardFile.withWriter { out ->
                out.println '-keepattributes InnerClasses, Signature, Annotations'
                findProguardFiles(variantOutputDir).each {
                    def trimmedContent = it.text.trim()
                    if (!trimmedContent.isEmpty()) {
                        out.println trimmedContent
                    }
                }
            }
        }
    }
}
