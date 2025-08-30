/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

plugins {
    id( "local.publishing-java-module" )
    id( "local.publishing-group-relocation" )
}

description = "Hibernate\'s community supported dialects"

dependencies {
    api( projects.hibernateCore )

    testImplementation( projects.hibernateTesting )
    testImplementation( project( path = ":hibernate-core", configuration = "tests" ) )
}

tasks.withType<Test> {
    include( "**/**" )
}
