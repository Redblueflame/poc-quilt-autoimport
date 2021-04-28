package com.redblueflame.importer;

import com.redblueflame.importer.experimental_importer.ExperimentalManager;
import com.redblueflame.importer.visitor.AccessRecord;
import com.redblueflame.importer.visitor.ImporterClassVisitor;
import org.apache.commons.io.IOUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.ClassReader;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public abstract class ImporterTransformer extends DefaultTask {
    private File input;
    private final ExperimentalManager manager = new ExperimentalManager();
    private final ImporterClassVisitor visitor = new ImporterClassVisitor();

    public ImporterTransformer() {
        try {
            this.mustRunAfter(this.getProject().getTasks().getByName("jar"));
            input = getInputName();
        } catch (Exception e) {
            e.printStackTrace();
            input = null;
        }
    }

    @TaskAction
    public void apply() {
        // Parse extensions
        try {
            getProject().getConfigurations().getByName("compile").getResolvedConfiguration().getFiles().forEach(file -> {
                try {
                    manager.import_file(new JarFile(file));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            File file = input;
            generatePath();
            manager.printExperimentalFunctions();
            processJar(file);
            HashMap<AccessRecord, String> errors = matchExperimental();
            List<ExperimentalException> exceptions = explainError(errors);
            if (exceptions != null && !exceptions.isEmpty()) {
                ExperimentalExceptionWrapper ex = new ExperimentalExceptionWrapper("Experimental functions are used but not enabled.", exceptions);
                ex.printMessages(getLogger());
                getLogger().error("\n\nTo enable the experimental features, please add the following lines to your " + AccessRecord.config_name + " file: \n" + generateTutorial(errors));
                throw ex;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void processJar(File input) throws IOException {
        JarFile jar = new JarFile(input);
        // Enumerate over entries
        Enumeration<JarEntry> jarIter = jar.entries();
        while (jarIter.hasMoreElements()) {
            JarEntry jarEntry = jarIter.nextElement();
            // Only treat it if it finished by .class (bytecode file)
            if (jarEntry.getName().endsWith(".class")) {
                getLogger().quiet("Processing: " + jarEntry.getName());
                InputStream inputStream = jar.getInputStream(jarEntry);
                byte[] sourceClassBytes = IOUtils.toByteArray(inputStream);
                ClassReader cr = new ClassReader(sourceClassBytes);
                cr.accept(visitor, 0);
            }
        }
    }

    @Nullable
    private List<ExperimentalException> explainError(HashMap<AccessRecord, String> errors) {
        List<ExperimentalException> exceptions = new ArrayList<>();
        if (errors.size() > 0) {
            errors.forEach((accessRecord, feature) -> exceptions.add(new ExperimentalException(accessRecord, feature)));
            return exceptions;
        }
        return null;
    }

    private HashMap<AccessRecord, String> matchExperimental() {
        HashMap<AccessRecord, String> result = new HashMap<>();
        for (AccessRecord id : this.visitor.methodVisitor.getAccesses()) {
            String val = this.manager.getExperimentalTag(id.descriptor);
            if (val != null) {
                result.put(id, val);
            }
        }
        return result;
    }

    private File getInputName() {
        return (File) getProject().getTasks().getByName("jar").getOutputs().getFiles().getFiles().toArray()[0];
    }
    private String generateTutorial(HashMap<AccessRecord, String> errors) {
        if (AccessRecord.config_name.equals("build.gradle")) {
            return generateTutorialGroovy(errors);
        } else {
            return generateTutorialKotlin(errors);
        }
    }
    private String generateTutorialGroovy(HashMap<AccessRecord, String> errors) {
        String features_str = getFeaturesString(errors);
        return "quilt {\n  //... \n  experimentalModules " + features_str + "\n}";
    }

    private String getFeaturesString(HashMap<AccessRecord, String> errors) {
        AtomicReference<String> features = new AtomicReference<>("");
        errors.forEach((accessRecord, feature) -> {
            features.getAndAccumulate("\"" + feature + "\", ", (s, s2) -> s += s2);
        });
        return features.get().substring(0, features.get().lastIndexOf(","));
    }

    private String generateTutorialKotlin(HashMap<AccessRecord, String> errors) {
        return "quilt {\n  //... \n  experimentalModules(" + getFeaturesString(errors) + ")\n}";
    }



    @SuppressWarnings("UnstableApiUsage")
    private void generatePath() {
        SourceSet mainSourceSet = getProject().getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().getByName("main");
        AccessRecord.basePath = mainSourceSet.getJava().getSourceDirectories().getAsPath();
        AccessRecord.config_name = getProject().getBuildFile().getName();
    }
}
