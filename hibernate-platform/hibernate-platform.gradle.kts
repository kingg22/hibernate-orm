plugins {
    id( "java-platform" )
    id( "local.module" )
    id( "local.publishing" )
}

description = "Platform (BOM) for Hibernate ORM dependencies"

dependencies {
    constraints {
        api( projects.hibernateCore )
        api( projects.hibernateTesting )

        api( projects.hibernateEnvers )
        api( projects.hibernateSpatial )
        api( projects.hibernateVector )

        api( projects.hibernateCommunityDialects )
        api( projects.hibernateScanJandex )

        api( projects.hibernateAgroal )
        api( projects.hibernateC3p0 )
        api( projects.hibernateHikaricp )

        api( projects.hibernateJcache )

        api( projects.hibernateMicrometer )
        api( projects.hibernateGraalvm )

        api( projects.hibernateProcessor )
        api( projects.hibernateGradlePlugin )
        api( projects.hibernateMavenPlugin )
        api( projects.hibernateAnt )

        api( libs.hibernateModels )

        api( libs.jakarta.jpa )
        api( libs.jakarta.jta )
        api( libs.jakarta.data )

        runtime( libs.antlrRuntime )
        runtime( libs.logging )
        runtime( libs.byteBuddy )
        runtime( libs.byteBuddyAgent )
        runtime( libs.jandex )
        runtime( libs.classmate )

        runtime( libs.jakarta.jaxb )
        runtime( libs.jakarta.jaxbApi )
        runtime( libs.jakarta.inject )


        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // todo : imo these are questionable
        runtime( libs.agroal )
        runtime( libs.agroalPool )
        runtime( libs.c3p0 )
        runtime( libs.hikaricp )

        runtime( libs.jcache )

        runtime( libs.micrometer )
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    }
}

val publishingExtension = project.getExtensions().getByType<PublishingExtension>()
publishingExtension.publications.named<MavenPublication>( "publishedArtifacts" ) {
    from( components["javaPlatform"] )
}
