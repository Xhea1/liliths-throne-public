pluginManagement {
	repositories {
		google()
		mavenCentral()
		gradlePluginPortal()
	}
}

dependencyResolutionManagement {
	repositories {
		google()
		mavenCentral()
		// The `core` module is built by Maven and installed to ~/.m2 via `mvn -pl core install`.
		mavenLocal()
	}
}

rootProject.name = "LilithsThroneAndroid"
include(":app")
