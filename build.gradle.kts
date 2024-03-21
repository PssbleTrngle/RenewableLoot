val mixin_extras_version: String by extra

plugins {
    id("com.possible-triangle.gradle") version ("0.1.4")
}

fabric()

repositories {
    modrinthMaven()
}

enablePublishing {
    githubPackages()
}

dependencies {
    "include"(implementation(annotationProcessor("io.github.llamalad7:mixinextras-fabric:${mixin_extras_version}")!!)!!)
}

uploadToCurseforge()
uploadToModrinth {
    syncBodyFromReadme()
}