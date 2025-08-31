/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

plugins {
    id( "local.publishing-java-module" )
    id( "local.publishing-group-relocation" )
}

description = "Integration for JDK JFR into Hibernate O/RM"

dependencies {
    implementation( projects.hibernateCore)

    testImplementation( projects.hibernateTesting )
    testImplementation( libs.test.jfrUnit )
}
