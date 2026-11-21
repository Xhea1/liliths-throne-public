plugins {
	id("com.android.application")
}

android {
	namespace = "com.lilithsthrone.android"
	// API 34 (Android 14) to match the target device.
	compileSdk = 34

	defaultConfig {
		applicationId = "com.lilithsthrone.android"
		// Android 14+ only: matches the target device, and guarantees the WebView supports the
		// :has() selector the response-box layout relies on.
		minSdk = 34
		targetSdk = 34
		versionCode = 1
		versionName = "0.4.11.3"
	}

	compileOptions {
		// Matches core/desktop's pinned maven.compiler.release.
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}

	buildTypes {
		release {
			isMinifyEnabled = false
		}
	}

	packaging {
		resources {
			// core ships a large SVG/icon resource tree; nothing to strip yet, but keep
			// duplicate-license noise from failing the build.
			excludes += setOf("META-INF/LICENSE*", "META-INF/NOTICE*")
		}
	}
}

/**
 * Stages the game's runtime data (dialogue XML, item/race definitions, fonts, ...) from the
 * repo-root res/ folder into the APK's assets, where AssetUnpacker copies it into private app
 * storage on first launch.
 *
 * Generated rather than committed so the ~48MB isn't duplicated in version control.
 *
 * Excluded:
 *   images/ - 387MB of character art, not reachable from any screen ported so far and would
 *             dominate APK size. Add once character rendering is implemented.
 *   mods/   - external mod loading isn't wired up on Android yet.
 */
val syncGameAssets by tasks.registering(Sync::class) {
	description = "Copies res/ (minus character art and mods) into the APK assets."
	from(rootProject.file("../res")) {
		exclude("images/**", "mods/**")
	}
	into(layout.projectDirectory.dir("src/main/assets/lt-res"))
}

tasks.named("preBuild") {
	dependsOn(syncGameAssets)
}

dependencies {
	// Pure-Java game logic, published by `mvn -pl core install` (see settings.gradle.kts).
	// NOTE: run `mvn -pl core install` before every Gradle build. The version never changes, so
	// without it Gradle keeps using the previously installed jar and edits to core never ship.
	implementation("innoxia:core:0.4.11.3") { isChanging = true }
	// Local HTTP server the WebView talks to.
	implementation("org.nanohttpd:nanohttpd:2.3.1")
	// DocumentFile, for the Storage-Access-Framework-backed save folder.
	implementation("androidx.documentfile:documentfile:1.0.1")
	implementation("androidx.appcompat:appcompat:1.6.1")
}
