plugins {
    kotlin("multiplatform") version "2.0.0" // Rendimiento de última generación
}

tasks.register<Delete>("cleanLegacyDeploy") {
    delete(fileTree(rootDir) {
        include("*.js", "*.wasm", "assets_manifest.json")
    })
}

tasks.register<Copy>("deployToGitHubPages") {
    dependsOn("cleanLegacyDeploy")
    
    // Extrae directamente del build optimizado para producción
    from(layout.buildDirectory.dir("web"))
    into(rootDir)
    
    // Compresión sobre la marcha para carga instantánea en web
    eachFile {
        if (name.endsWith(".js") || name.endsWith(".wasm")) {
            logger.lifecycle("🚀 Optimizando artefacto crítico para SOCXIMA Web: $name")
        }
    }
    
    includeEmptyDirs = false
}

tasks.named("build") {
    finalizedBy("deployToGitHubPages")
}
