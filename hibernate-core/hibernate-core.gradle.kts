/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.jvm.tasks.Jar
import org.gradle.api.tasks.testing.Test

plugins {
    id( "local.publishing-java-module" )
    id( "local.publishing-group-relocation" )

    id( "org.hibernate.orm.antlr" )
    id( "local-xjc-plugin" )
}

description = "Hibernate's core ORM functionality"

configurations {
    register( "tests" ) {
        description = "Configuration for the produced test jar"
    }
}

dependencies {
    api( libs.jakarta.jpa )
    api( libs.jakarta.jta )

    api( projects.hibernateCoreApi )
    api( projects.hibernateCoreAnnotations )
    api( projects.hibernateCoreBytecode )

    implementation( libs.hibernateModels )
    implementation( libs.classmate )
    implementation( libs.byteBuddy )

    implementation( libs.jakarta.jaxbApi )
    implementation( libs.jakarta.jaxb )
    implementation( libs.jakarta.inject )

    implementation( libs.antlrRuntime )

    compileOnly( libs.jakarta.jacc )
    compileOnly( libs.jakarta.validation )
    compileOnly( libs.jakarta.cdi )
    compileOnly( libs.jakarta.jsonbApi )
    compileOnly( libs.jackson )
    compileOnly( libs.jacksonXml )
    compileOnly( libs.jdbc.postgresql )
    compileOnly( libs.jdbc.edb )

    testImplementation( projects.hibernateTesting )
    testImplementation( projects.hibernateAnt )
    testImplementation( projects.hibernateScanJandex )

    testImplementation( libs.test.shrinkwrap )
    testImplementation( libs.test.shrinkwrapDescriptors )
    testImplementation( libs.jakarta.cdi )
    testImplementation( libs.jakarta.jacc )
    testImplementation( libs.jakarta.validation )
    testImplementation( libs.test.validator ) {
        isTransitive = true
    }
    testImplementation("joda-time:joda-time:2.3" )
    testImplementation( libs.jdbc.h2 )
    testImplementation( libs.hibernateModelsJandex )

    testRuntimeOnly( libs.byteBuddy )
    testRuntimeOnly( libs.test.weld )
    testRuntimeOnly( libs.test.wildFlyTxnClient )
    testImplementation( libs.jandex )
    testImplementation( libs.jakarta.jsonb )
    testImplementation( libs.jackson )
    testRuntimeOnly( libs.jacksonXml )
    testRuntimeOnly( libs.jacksonJsr310 )

    testAnnotationProcessor( projects.hibernateProcessor )

    antlr( libs.antlr )
    antlr( libs.antlrRuntime )
}

tasks.jar {
    manifest {
        attributes( "Main-Class" to "org.hibernate.Version" )
    }
}

tasks.withType<JavaCompile>().configureEach {
    val pathSeparator = File.pathSeparator

    val patchPaths = listOf(
        project(":hibernate-core-annotations").sourceSets.main.get().output.asPath,
        project(":hibernate-core-api").sourceSets.main.get().output.asPath,
        project(":hibernate-core-bytecode").sourceSets.main.get().output.asPath,
    ).joinToString(pathSeparator)

    // need to patch modules of JPMS to add gradle modules (internal split) into the publishing module of hibernate-core
    // all gradle modules of hibernate core is unnamed module because JPMS prohibits split package
    options.compilerArgs.addAll(listOf("--patch-module", "org.hibernate.orm.core=$patchPaths"))
}

tasks.named<Jar>( "jar" ) {
    from( project(":hibernate-core-api").sourceSets.main.get().output )
    from( project(":hibernate-core-annotations").sourceSets.main.get().output )
    from( project(":hibernate-core-bytecode").sourceSets.main.get().output )
}

sourceSets {
    named( "test" ) {
        resources {
            srcDir( "src/test/resources" )
            srcDir( "src/test/bundles" )
        }
    }
}

xjc {
    outputDirectory = layout.buildDirectory.dir( "generated/sources/xjc/main" ).get().asFile

    schemas {
        create( "cfg" ) {
            xsdFile = file("src/main/resources/org/hibernate/xsd/cfg/legacy-configuration-4.0.xsd")
            xjcBindingFile = file("src/main/xjb/hbm-configuration-bindings.xjb")
        }
        create( "hbm" ) {
            xsdFile = file("src/main/resources/org/hibernate/xsd/mapping/legacy-mapping-4.0.xsd")
            xjcBindingFile = file("src/main/xjb/hbm-mapping-bindings.xjb")
            xjcExtensions("inheritance", "simplify")
        }
        create( "configuration" ) {
            xsdFile = file("src/main/resources/org/hibernate/xsd/cfg/configuration-3.2.0.xsd")
            xjcBindingFile = file("src/main/xjb/configuration-bindings.xjb")
            xjcExtensions("inheritance", "simplify")
        }
        create( "mapping" ) {
            xsdFile = file("src/main/resources/org/hibernate/xsd/mapping/mapping-7.0.xsd")
            xjcBindingFile = file("src/main/xjb/mapping-bindings.xjb")
            xjcExtensions("inheritance", "simplify")
        }
    }
}

val db = project.extra["db"] as String
val dbHost = project.extra["dbHost"] as String
@Suppress("UNCHECKED_CAST")
val dbBundle = project.extra["dbBundle"] as Map<String, Map<String, String>>


val copyBundleResourcesXml = tasks.register<Copy>( "copyBundleResourcesXml" ) {
    inputs.property("db", db)
    inputs.property("dbHost", dbHost)

    val bundlesTargetDir = layout.buildDirectory.dir("bundles")
    val bundleTokens = dbBundle[db]?.mapValues { it.value.replace("&", "&amp;") }?.toMutableMap() ?: mutableMapOf()
    bundleTokens["buildDirName"] = project.relativePath(layout.buildDirectory.get().asFile)

    from("src/test/bundles/templates") {
        include("**/*.xml")
    }
    into(bundlesTargetDir)
    filter<ReplaceTokens>("tokens" to bundleTokens)

    doFirst {
        bundlesTargetDir.get().asFile.mkdirs()
    }
}

val copyBundleResourcesNonXml = tasks.register<Copy>( "copyBundleResourcesNonXml" ) {
    inputs.property("db", db)

    val bundlesTargetDir = layout.buildDirectory.dir("bundles")
    val bundleTokens = dbBundle[db]?.toMutableMap() ?: mutableMapOf()
    bundleTokens["buildDirName"] = project.relativePath(layout.buildDirectory.get().asFile)

    from( "src/test/bundles/templates" ) {
        exclude( "**/*.xml" )
    }
    into(bundlesTargetDir)
    filter<ReplaceTokens>( "tokens" to bundleTokens )

    doFirst {
        bundlesTargetDir.get().asFile.mkdirs()
    }
}

val copyBundleResources = tasks.register( "copyBundleResources" ) {
    dependsOn(copyBundleResourcesXml, copyBundleResourcesNonXml)
}

tasks.named<ProcessResources>( "processTestResources" ) {
    dependsOn(copyBundleResources)
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.named<Jar>( "sourcesJar" ) {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    dependsOn(
        ":hibernate-core:generateGraphParser",
        ":hibernate-core:generateHqlParser",
        ":hibernate-core:generateSqlScriptParser",
        ":hibernate-core:generateOrderingParser",
    )
}

val testJar = tasks.register<Jar>( "testJar" ) {
    dependsOn( tasks.named( "testClasses" ) )
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveClassifier.set( "test" )
    from(sourceSets.test.get().output)
}

artifacts.add( "tests", testJar )

tasks.register<JavaCompile>( "generateAnnotationClasses" ) {
    description = "Generate concrete classes for Hibernate and JPA annotations"

    source = sourceSets.main.get().allJava
    include( "org/hibernate/annotations/*" )
    classpath = sourceSets.main.get().runtimeClasspath + sourceSets.main.get().compileClasspath
    options.annotationProcessorPath = sourceSets.main.get().compileClasspath
    options.compilerArgs = listOf(
        "-proc:only",
        "-processor",
        "org.hibernate.orm.build.annotations.ClassGeneratorProcessor"
    )

    destinationDirectory.set( layout.buildDirectory.dir( "generated/sources/annotations/" ) )
}

tasks.register<JavaCompile>( "generateEnversStaticMetamodel" ) {
    source = sourceSets.main.get().java
    include(
        "org/hibernate/envers/DefaultRevisionEntity.java",
        "org/hibernate/envers/DefaultTrackingModifiedEntitiesRevisionEntity.java",
        "org/hibernate/envers/enhanced/SequenceIdRevisionEntity.java",
        "org/hibernate/envers/enhanced/SequenceIdTrackingModifiedEntitiesRevisionEntity.java",
    )

    classpath = sourceSets.main.get().runtimeClasspath + sourceSets.test.get().compileClasspath
    options.compilerArgs = listOf(
        "-proc:only",
        "-processor",
        "org.hibernate.processor.HibernateProcessor"
    )

    destinationDirectory.set( file( "${projectDir}/src/main/java" ) )
}

tasks.withType<Test>().configureEach {
    systemProperties(
        mapOf(
            "file.encoding" to "utf-8",
            "hsqldb.method_class_names" to "org.hibernate.orm.test.jpa.transaction.TransactionTimeoutTest.sleep"
        )
    )
    jvmArgs = listOf(
        "--add-opens", "java.base/java.nio.charset=ALL-UNNAMED",
        "--add-opens", "java.base/java.security=ALL-UNNAMED",
        "--add-opens", "java.base/java.lang=ALL-UNNAMED",
        "-Dlog4j2.disableJmx=true"
    )
    if (project.db == "h2" || project.db == "hsqldb") {
        maxParallelForks = Runtime.getRuntime().availableProcessors().div(2).coerceAtLeast(1)
    }
}

tasks.named<Javadoc>("javadoc") {
    options {
        overview = rootProject.file("shared/javadoc/overview.html").name
        exclude("**/internal/**", "org/hibernate/boot/jaxb/**", "org/hibernate/tuple/**")
    }
}
