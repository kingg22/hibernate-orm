/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

plugins {
    id("local.publishing-java-module")
    id("local.publishing-group-relocation")
    id("org.hibernate.build.version-injection")
}

description = "Hibernate compile-time tooling"

sourceSets {
    register("quarkusOrmPanache") {
        java {
            setSrcDirs(listOf("src/quarkusOrmPanache/java"))
        }
        resources {
            setSrcDirs(sourceSets["main"].resources.srcDirs)
        }
        compileClasspath += sourceSets["main"].output + sourceSets["test"].output
        runtimeClasspath += sourceSets["main"].output + sourceSets["test"].output
    }
    register("quarkusHrPanache") {
        java {
            setSrcDirs(listOf("src/quarkusHrPanache/java"))
        }
        resources {
            setSrcDirs(sourceSets["main"].resources.srcDirs)
        }
        compileClasspath += sourceSets["main"].output + sourceSets["test"].output
        runtimeClasspath += sourceSets["main"].output + sourceSets["test"].output
    }
    register("jakartaData") {
        java {
            setSrcDirs(listOf("src/jakartaData/java"))
        }
        resources {
            setSrcDirs(sourceSets["main"].resources.srcDirs)
        }
        compileClasspath += sourceSets["main"].output + sourceSets["test"].output
        runtimeClasspath += sourceSets["main"].output + sourceSets["test"].output
    }
}

dependencies {
    // api - ewww... but Maven needs them this way
    api( projects.hibernateCore )
    api( libs.hibernateModels )
    api( libs.jandex )
    api( libs.jakarta.jaxbApi )
    api( libs.jakarta.jaxb )
    api( libs.jakarta.validation )
    api( libs.jakarta.annotation )
    api( libs.antlrRuntime )
    api( libs.byteBuddy )
    api( libs.logging )

    "quarkusOrmPanacheImplementation"("io.quarkus:quarkus-hibernate-orm-panache:3.24.1")
    "quarkusHrPanacheImplementation"("io.quarkus:quarkus-hibernate-reactive-panache:3.24.1")
    "jakartaDataImplementation"("jakarta.data:jakarta.data-api:1.0.0")
    "jakartaDataImplementation"("org.hibernate.reactive:hibernate-reactive-core:3.0.1.Final")
    "jakartaDataImplementation"("io.quarkus:quarkus-hibernate-orm-panache:3.24.1")
}

configurations {
    named("quarkusOrmPanacheImplementation") {
        extendsFrom( configurations.testImplementation.get() )
    }
    named("quarkusOrmPanacheRuntimeOnly") {
        extendsFrom( configurations.testRuntimeOnly.get() )
    }
    named("quarkusOrmPanacheCompileOnly") {
        extendsFrom( configurations.testCompileOnly.get() )
    }

    named("quarkusHrPanacheImplementation") {
        extendsFrom( configurations.testImplementation.get() )
    }
    named("quarkusHrPanacheRuntimeOnly") {
        extendsFrom( configurations.testRuntimeOnly.get() )
    }
    named("quarkusHrPanacheCompileOnly") {
        extendsFrom( configurations.testCompileOnly.get() )
    }

    named("jakartaDataImplementation") {
        extendsFrom( configurations.testImplementation.get() )
    }
    named("jakartaDataRuntimeOnly") {
        extendsFrom( configurations.testRuntimeOnly.get() )
    }
    named("jakartaDataCompileOnly") {
        extendsFrom( configurations.testCompileOnly.get() )
    }
}

val quarkusOrmPanacheTest by tasks.registering(Test::class) {
    description = "Runs the Quarkus ORM Panache tests."
    group = "verification"
    useJUnitPlatform()

    testClassesDirs = sourceSets["quarkusOrmPanache"].output.classesDirs
    classpath = sourceSets["quarkusOrmPanache"].runtimeClasspath
    shouldRunAfter( tasks.test )
}

val quarkusHrPanacheTest by tasks.registering(Test::class) {
    description = "Runs the Quarkus HR Panache tests."
    group = "verification"
    useJUnitPlatform()

    testClassesDirs = sourceSets["quarkusHrPanache"].output.classesDirs
    classpath = sourceSets["quarkusHrPanache"].runtimeClasspath
    shouldRunAfter( tasks.test )
}

val jakartaDataTest by tasks.registering(Test::class) {
    description = "Runs the Jakarta Data tests."
    group = "verification"
    useJUnitPlatform()

    testClassesDirs = sourceSets["jakartaData"].output.classesDirs
    classpath = sourceSets["jakartaData"].runtimeClasspath
    shouldRunAfter( tasks.test )
}

tasks.check {
    dependsOn( quarkusHrPanacheTest, quarkusOrmPanacheTest, jakartaDataTest )
}

tasks.test {
    dependsOn( quarkusHrPanacheTest, quarkusOrmPanacheTest, jakartaDataTest )
}

tasks.named( "sourcesJar" ) {
    dependsOn( ":hibernate-core:generateHqlParser", ":hibernate-core:generateSqlScriptParser" )
}

tasks.compileTestJava {
    options.compilerArgs.addAll(
            listOf(
                    "-proc:none",
                    "-AsuppressJakartaDataMetamodel=true"
            )
    )
}

val publishingExtension = extensions.getByType<PublishingExtension>()
val oldGroupId = "org.hibernate"
val oldArtifactId = "hibernate-jpamodelgen"

publishingExtension.publications.named<MavenPublication>( "groupRelocation" ) {
    artifactId = oldArtifactId
    pom {
        name.set("Relocation : $oldGroupId:$oldArtifactId -> ${project.group}:${project.name}")
        description.set("The `$oldArtifactId` module has been renamed `${project.name}` and moved to the `${project.group}` group-id")
    }
}

publishingExtension.publications.register<MavenPublication>( "renameRelocation" ) {
    artifactId = oldArtifactId
    pom {
        name.set("Relocation : ${project.group}:$oldArtifactId -> ${project.group}:${project.name}")
        description.set("The `$oldArtifactId` module has been renamed `${project.name}`")

        distributionManagement {
            relocation {
                groupId.set(project.group.toString())
                artifactId.set(project.name)
                version.set(project.version.toString())
            }
        }
    }
}

tasks.named( "forbiddenApisJakartaData" ) {
    enabled = false
}
tasks.named( "forbiddenApisQuarkusOrmPanache" ) {
    enabled = false
}
tasks.named( "forbiddenApisQuarkusHrPanache" ) {
    enabled = false
}
