/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
plugins {
	id( "local.module" ) apply false

	alias( libs.plugins.hibernate.build.version.injection ) apply false
	id( "org.hibernate.orm.database-service" ) apply false
	alias( libs.plugins.biz.aQute.bnd ) apply false

    alias( libs.plugins.forbiddenapis ) apply false
	alias( libs.plugins.spotless ) apply false
	alias( libs.plugins.checkerframework ) apply false
	id( "org.hibernate.orm.build.jdks" ) apply false

	alias( libs.plugins.gradle.nexus.publish ) apply false

	idea
	alias( libs.plugins.idea.ext )
    eclipse
	alias( libs.plugins.task.tree )
}


// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// Releasing

tasks.register( "releasePrepare" ) {
	group = "release-prepare"
	description = "Scripted release 'Release Prepare' stage.  " +
			"Includes various checks as to the publish-ability of the project: testing, generation, etc.  " +
			"Sub-projects register their own `releasePrepare` to hook into this stage."
	// See `:release:releasePrepare` which does a lot of heavy lifting here
}

tasks.register( "releasePerform" ) {
	group = "release-perform"
	description = "Scripted release 'Release Perform' stage.  " +
			"Generally this entails publishing artifacts to various servers.  " +
			"Sub-projects register their own `releasePerform` to hook into this stage."
	// See `:release:releasePerform` which does a lot of heavy lifting here
}

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// CI Build Task

tasks.register( "ciBuild" ) {
	description = "The task performed when one of the 'main' jobs are triggered on the " +
			"CI server.  Just as above, relies on the fact that subprojects will " +
			"appropriately define a release task themselves if they have any tasks " +
			"which should be performed from these CI jobs"
}

idea {
	module {
		name = "hibernate-orm"
	}
}
