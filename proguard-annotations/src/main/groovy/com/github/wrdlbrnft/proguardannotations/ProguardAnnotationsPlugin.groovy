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
        if(!project.plugins.findPlugin('com.neenbedankt.android-apt')) {
            throw new ProjectConfigurationException('Android APT Plugin is required for ProguardAnnotations to work. ' +
                    'You need to apply it before applying proguard-annotations', null)
        }

        def rootOutputDir = project.file(new File(project.buildDir, "generated/source/apt"))
        def variants = determineVariants(project)
        project.afterEvaluate {
            project.android[variants].all { variant ->
                def variantOutputDir = new File(rootOutputDir, variant.dirName)
                def javaCompile = variant.hasProperty('javaCompiler') ? variant.javaCompiler : variant.javaCompile
                javaCompile.doLast {
                    println ''
                    println '######################'
                    variantOutputDir.listFiles().each {
                        println it.name
                    }
                    println '######################'
                    println ''
                }
            }
        }

        project.dependencies {
            compile 'com.github.wrdlbrnft:proguard-annotations-api:0.1.0.5'
            apt 'com.github.wrdlbrnft:proguard-annotations-processor:0.1.0.5'
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
}
