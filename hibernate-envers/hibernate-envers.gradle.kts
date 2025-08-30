/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

import org.gradle.api.file.DuplicatesStrategy

plugins {
    id( "local.publishing-java-module" )
    id( "local.publishing-group-relocation" )
}

description = "Hibernate's entity version (audit/history) support"

dependencies {
    api( projects.hibernateCore )

    implementation( libs.hibernateModels )
    implementation( libs.jakarta.jaxbApi )
    implementation( libs.jakarta.jaxb )
    implementation( libs.jandex )
    implementation( libs.hibernateModels )

    compileOnly( libs.ant )

    annotationProcessor( projects.hibernateProcessor )
    compileOnly( libs.jakarta.annotation )

    testImplementation( projects.hibernateTesting )

    testAnnotationProcessor( projects.hibernateProcessor )
}

tasks.withType<Test> {
    include( "**/**" )
}

sourceSets {
    test {
        java {
            srcDir( file( "src/demo/java" ) )
        }
        resources {
            srcDir(  file( "src/demo/resources" ) )
        }
    }
}

configurations {
    register( "tests" ) {
        description = "Configuration for the produced test jar"
    }
}

tasks.withType<Test>().forEach { test ->
    if ( project.db == "h2" || project.db == "hsqldb" ) {
        // Parallel test runs when running with in-memory databases
        test.maxParallelForks = Runtime.getRuntime()?.availableProcessors()?.div( 2 ) ?: 1
    }
}

tasks.named<Jar>( "sourcesJar" ) {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val testJarTask = tasks.register<Jar>( "testJar" ) {
    dependsOn( tasks.named( "testClasses" ) )
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveClassifier.set( "test" )
    from( sourceSets.test.get().output )
}

artifacts.add( "tests", testJarTask )
