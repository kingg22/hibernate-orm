plugins {
    id( "local.publishing-java-module" )
    id( "local.publishing-group-relocation" )
}

description = "Integration for javax.cache into Hibernate as a second-level caching service"

dependencies {
    api( projects.hibernateCore )
    api( libs.jcache )

    testImplementation( projects.hibernateTesting )

    testRuntimeOnly( libs.ehcache3 ) {
        capabilities {
            requireCapability( "org.ehcache.modules:ehcache-xml-jakarta" )
        }
    }
}
