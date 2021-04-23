package com.redblueflame.importer;

import com.redblueflame.importer.experimental_importer.ExperimentalManager;
import com.redblueflame.importer.visitor.AccessRecord;
import com.redblueflame.importer.visitor.ImporterClassVisitor;
import org.apache.commons.io.IOUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public abstract class ImporterTransformer extends DefaultTask {
    private File input;
    private final ExperimentalManager manager = new ExperimentalManager();
    private final ImporterClassVisitor visitor = new ImporterClassVisitor(Opcodes.ASM4);

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
            processJar(file);
            if (explainError()) {
                throw new GradleException("There were errors, please look at the build output.");
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
                InputStream inputStream = jar.getInputStream(jarEntry);
                byte[] sourceClassBytes = IOUtils.toByteArray(inputStream);
                ClassReader cr = new ClassReader(sourceClassBytes);
                cr.accept(visitor, 0);
            }
        }
    }

    private boolean explainError() {
        HashMap<AccessRecord, String> errors = matchExperimental();
        if (errors.size() > 0) {
            getLogger().error("\n\n");
            errors.forEach((accessRecord, val) -> {
                getLogger().error("ERROR - Experimental function usage\n" +
                        "At " + accessRecord.scope.replace("/", ".") + "\n" +
                        "The feature " + val + "  is marked as experimental." +
                        "To use it, add the following settings to your build.gradle: \n"+
                        "enableExperimentalFeature '" + val + "'\n");
            });
            return true;
        }
        return false;
    }

    private HashMap<AccessRecord, String> matchExperimental() {
        HashMap<AccessRecord, String> result = new HashMap<>();
        for (AccessRecord id : this.visitor.methodVisitor.getAccesses()) {
            String val = this.manager.getExperimentalTag(id.method);
            if (val != null) {
                result.put(id, val);
            }
        }
        return result;
    }

    private File getInputName() {
        return (File) getProject().getTasks().getByName("jar").getOutputs().getFiles().getFiles().toArray()[0];
    }
}
