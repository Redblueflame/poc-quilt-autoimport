package com.redblueflame.importer;


import org.gradle.api.Plugin;
import org.gradle.api.Project;


public class ImporterPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        System.out.println(":applied Importer");

        project.getExtensions().create("importer", ImporterSettings.class);
        project.getTasks().register("addDependencies", ImporterTransformer.class, t -> {
            t.setDescription("Adds the needed dependencies to the quilt.mod.json file.");
            t.dependsOn("jar");
            t.setGroup("quilt");
        });
    }
}
