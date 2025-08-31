/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven( "${gradle.gradleUserHomeDir}/tmp/plugins" ) {
            name = "localPluginRepository"
        }
        mavenCentral()
    }

    includeBuild( "local-build-plugins" )
}

plugins {
    id( "org.hibernate.orm.build.env-settings" )
    id( "org.hibernate.orm.build.jdks-settings" )
    id( "com.gradle.develocity" ) version "4.0.2"
    id( "com.gradle.common-custom-user-data-gradle-plugin" ) version "2.3"
}

rootProject.name = "hibernate-orm"
enableFeaturePreview( "TYPESAFE_PROJECT_ACCESSORS" )

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        /*
        if ( rootProject.hasProperty( "mavenMirror" ) ) {
            maven( rootProject.property( "mavenMirror" )!! )
        }
         */

        mavenCentral()

        if ( System.getProperty("JPA_PREVIEW") != null ) {
			maven( "https://jakarta.oss.sonatype.org/content/repositories/releases/" )
            // Needed for the SNAPSHOT versions of Jakarta Persistence
            maven( "https://jakarta.oss.sonatype.org/content/repositories/snapshots/" )
		}

        //Allow loading additional dependencies from a local path;
        //useful to load JDBC drivers which can not be distributed in public.
        val additionalRepo = System.getenv("ADDITIONAL_REPO")
        if ( additionalRepo != null ) {
            flatDir {
                dirs( additionalRepo )
            }
        }
    }

    versionCatalogs {
        create( "jdks" ) {
            // see gradle.properties (or set with -D/-P)
            val baseJdk = jdkVersions.baseline.toString()
            val minJdk = jdkVersions.min.toString()
            val maxJdk = jdkVersions.max.toString()

            version( "baseline", baseJdk )
            version( "compatible", "17 or 21" )
            version( "jdbc", "4.2" ) // Bundled with JDK 11

            // We may require a minimum JDK version, for consistency across builds
            version( "minSupportedJdk", minJdk )
            // Gradle does bytecode transformation on tests.
            // You can't use bytecode higher than what Gradle supports, even with toolchains.
            version( "maxSupportedBytecode", maxJdk )
        }
        create( "libs" ) {
            // `jakartaJpaVersion` comes from the local-build-plugins to allow for command-line overriding of the JPA version to use
            val jpaVersion = version( "jpa", jakartaJpaVersion)

            library( "jakarta.jpa", "jakarta.persistence", "jakarta.persistence-api" ).versionRef( jpaVersion )

            // overrideable versions
            /* TODO
            val jdbcH2 = settings.extra.get("gradle.libs.versions.h2")?.toString()
            if ( jdbcH2 != null ) {
                version( "jdbc-h2", jdbcH2 )
            }

            val jdbcDerby = settings.extra.get("gradle.libs.versions.derby")?.toString()
            if ( jdbcDerby != null ) {
                version( "jdbc-derby", jdbcDerby )
            }

            val jdbcH2gis = settings.extra.get("gradle.libs.versions.h2gis")?.toString()
            if ( jdbcH2gis != null ) {
                version( "jdbc-h2gis", jdbcH2gis )
            }

            val jdbcHsqldb = settings.extra.get("gradle.libs.versions.hsqldb")?.toString()
            if ( jdbcHsqldb != null ) {
                version( "jdbc-hsqldb", jdbcHsqldb )
            }
             */
        }
    }
}

apply(from = file( "gradle/gradle-develocity.gradle" ) )

if ( !JavaVersion.current().isJava11Compatible ) {
    throw GradleException( "Gradle must be run with Java 11 or later" )
}

buildCache {
    local {
        // do not use local build cache for CI jobs, period!
        isEnabled = !extra["isCiEnvironment"].toString().toBoolean()
    }
    remote( develocity.buildCache ) {
        isEnabled = extra["useRemoteCache"].toString().toBoolean()
        // Check access key presence to avoid build cache errors on PR builds when access key is not present
        val accessKey = System.getenv("DEVELOCITY_ACCESS_KEY")
        setPush ( settings.extra["populateRemoteBuildCache"].toString().toBoolean() && accessKey != null )
    }
}

include(
    "hibernate-core",
    "hibernate-core-api",
    "hibernate-core-annotations",
    "hibernate-core-bytecode",
    "hibernate-core-internal",
    "hibernate-core-service",
    "hibernate-testing",
    "hibernate-envers",
    "hibernate-spatial",
    "hibernate-platform",
    "hibernate-community-dialects",
    "hibernate-vector",
    "hibernate-c3p0",
    "hibernate-hikaricp",
    "hibernate-agroal",
    "hibernate-jcache",
    "hibernate-micrometer",
    "hibernate-graalvm",
    "hibernate-integrationtest-java-modules",
    "documentation",
    "release",
    "hibernate-scan-jandex",
    "metamodel-generator",
    "hibernate-gradle-plugin",
    "hibernate-maven-plugin",
    "hibernate-ant",
)

// Not all JDK implementations support JFR
if ( "OpenJDK Runtime Environment" == System.getProperty("java.runtime.name") ) {
    include( "hibernate-jfr" )
}

val metamodelGenerator = project( ":metamodel-generator" )
metamodelGenerator.projectDir = File( rootProject.projectDir, "tooling/metamodel-generator" )
metamodelGenerator.name = "hibernate-processor"

project( ":hibernate-gradle-plugin" ).projectDir = File( rootProject.projectDir, "tooling/hibernate-gradle-plugin" )

project( ":hibernate-maven-plugin" ).projectDir = File( rootProject.projectDir, "tooling/hibernate-maven-plugin" )

project( ":hibernate-ant" ).projectDir = File( rootProject.projectDir, "tooling/hibernate-ant" )

project( ":hibernate-core-api" ).projectDir = File( rootProject.projectDir, "hibernate-core-modules/hibernate-core-api" )

project( ":hibernate-core-annotations" ).projectDir = File( rootProject.projectDir, "hibernate-core-modules/hibernate-core-annotations" )

project( ":hibernate-core-bytecode" ).projectDir = File( rootProject.projectDir, "hibernate-core-modules/hibernate-core-bytecode" )

project( ":hibernate-core-internal" ).projectDir = File( rootProject.projectDir, "hibernate-core-modules/hibernate-core-internal" )

project( ":hibernate-core-service" ).projectDir = File( rootProject.projectDir, "hibernate-core-modules/hibernate-core-service" )

rootProject.children.forEach { project ->
    assert( project.projectDir.isDirectory )
    val ktsFile = File(project.projectDir, "${project.name}.gradle.kts")
    val groovyFile = File(project.projectDir, "${project.name}.gradle")
    val buildKtsFile = File(project.projectDir, "build.gradle.kts")
    val buildGroovyFile = File(project.projectDir, "build.gradle")

    project.buildFileName = when {
        ktsFile.exists() -> ktsFile.name
        groovyFile.exists() -> groovyFile.name
        buildKtsFile.exists() -> buildKtsFile.name
        buildGroovyFile.exists() -> buildGroovyFile.name
        else -> error("Expected build file for '${project.name}'")
    }
}
