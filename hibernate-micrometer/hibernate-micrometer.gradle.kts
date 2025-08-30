plugins {
    id( "local.publishing-java-module" )
    id( "local.publishing-group-relocation" )
}

description = "Integration for Micrometer metrics into Hibernate as a metrics collection package"

dependencies {
    implementation( projects.hibernateCore )
    implementation( libs.micrometer )

    testImplementation( projects.hibernateTesting )

    testAnnotationProcessor( projects.hibernateProcessor )
    testCompileOnly( libs.jakarta.annotation )
}

sourceSets {
    // resources inherently exclude sources
    test {
        resources {
            setSrcDirs( listOf( "src/test/resources") )
        }
    }
}
