/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

plugins {
    id( "local.publishing-java-module" )
    id( "local.publishing-group-relocation" )
}

description = "Integration for c3p0 Connection pooling into Hibernate ORM"

dependencies {
    implementation( projects.hibernateCore )
    implementation( libs.c3p0 )

    testImplementation( projects.hibernateTesting )
    testImplementation( libs.jakarta.validation )
    testImplementation( libs.test.validator )
}
