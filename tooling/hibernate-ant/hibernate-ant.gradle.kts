/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

plugins {
    id( "local.publishing-java-module" )
    id( "local.publishing-group-relocation" )
    id( "org.hibernate.build.version-injection" )
}

description = "Annotation Processor to generate JPA 2 static metamodel classes"

dependencies {
    compileOnly( libs.ant )
    implementation( projects.hibernateCore )
    testImplementation( libs.ant )
}
