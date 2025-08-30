/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

plugins {
	id( "local.publishing-java-module" )
	id( "local.publishing-group-relocation" )
}

description = "Integrate support for Spatial/GIS data into Hibernate O/RM"

dependencies {
	api( projects.hibernateCore )
	api( libs.geolatte )

	compileOnly( libs.jdbc.postgresql )

	testImplementation( projects.hibernateTesting )
	testImplementation( projects.hibernateAnt )
	testImplementation( project( path = ":hibernate-core", configuration = "tests" ) )
	testImplementation( libs.jakarta.validation )
	testImplementation( libs.jandex )
	testImplementation( libs.classmate )
	testImplementation( libs.test.validator )
	testImplementation( "org.dom4j:dom4j:2.1.3@jar" )

	testImplementation( libs.jdbc.postgresql )
	testImplementation( libs.jdbc.h2gis )

	testRuntimeOnly( "jaxen:jaxen:1.1" )
	testRuntimeOnly( libs.byteBuddy )
}


sourceSets.test.get().resources {
	setSrcDirs( listOf( "src/test/resources") )
}

tasks.test {
	enabled = listOf(
        "h2",
        "pgsql",
        "pgsql_ci",
        "cockroachdb",
        "mariadb",
        "mariadb_ci",
        "mysql",
        "mysql_ci",
        "oracle",
        "oracle_ci",
        "oracle_xe_ci",
        "mssql",
        "mssql_ci"
    ).contains( project.db )
}

