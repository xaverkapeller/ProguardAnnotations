package com.github.wrdlbrnft.proguardannotations

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import org.gradle.api.tasks.compile.GroovyCompile

/**
 * Created by Xaver on 02/04/16.
 */
class ProguardAnnotationsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def variants = determineVariants(project)
        def configuration = project.configurations
                .create('proguardprocessor')
                .extendsFrom(project.configurations.compile, project.configurations.provided)

        project.dependencies {
            compile 'com.github.wrdlbrnft:proguard-annotations-api:0.1.0.1'
            proguardprocessor 'com.github.wrdlbrnft:proguard-annotations-processor:0.1.0.1'
        }

        project.afterEvaluate {
            project.android[variants].all { variant ->
                configureVariant(project, variant, configuration)
            }
        }
    }

    private static String determineVariants(Project project) {
        if (project.plugins.findPlugin("com.android.application") || project.plugins.findPlugin("android") || project.plugins.findPlugin("com.android.test")) {
            return "applicationVariants";
        } else if (project.plugins.findPlugin("com.android.library") || project.plugins.findPlugin("android-library")) {
            return "libraryVariants";
        } else {
            throw new ProjectConfigurationException("The android or android-library plugin must be applied to the project", null)
        }
    }

    private static void configureVariant(def project, def variant, def configuration) {
        if (configuration.empty) {
            project.logger.info("No progurd dependencies for configuration ${configuration.name}");
            return;
        }

        def rootOutputDir = project.file(new File(project.buildDir, "generated/proguard"))
        def variantOutputDir = new File(rootOutputDir, variant.dirName)

        def javaCompile = variant.hasProperty('javaCompiler') ? variant.javaCompiler : variant.javaCompile

        def taskDependency = configuration.buildDependencies
        if (taskDependency) {
            javaCompile.dependsOn += taskDependency
        }

        javaCompile.options.compilerArgs += [
                '-s', variantOutputDir
        ]

        javaCompile.doFirst {
            variantOutputDir.mkdirs()
        }

        javaCompile.doLast {
            println ''
            println '######################'
            variantOutputDir.listFiles().each {
                println it.name
            }
            println '######################'
            println ''
        }

        def dependency = javaCompile.finalizedBy;
        def dependencies = dependency.getDependencies(javaCompile);
        for (def dep : dependencies) {
            if (dep instanceof GroovyCompile) {
                if (dep.groovyOptions.hasProperty("javaAnnotationProcessing")) {
                    dep.options.compilerArgs += javaCompile.options.compilerArgs;
                    dep.groovyOptions.javaAnnotationProcessing = true
                }
            }
        }
    }
}
