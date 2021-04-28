plugins {
    kotlin("jvm") version "1.4.32"
}

buildscript{
    dependencies{
        classpath(files("../classvisitor/build/libs/classvisitor-1.0-SNAPSHOT-all.jar"))
    }
}

version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "Quilt"
        url = uri("https://maven.quiltmc.org/repository/release")
    }
}
apply<com.redblueflame.importer.ImporterPlugin>()
dependencies {
    compile(project(":testlibrary"))
    compile(kotlin("stdlib"))
    compile(project(":classvisitor"))
}