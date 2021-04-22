package com.redblueflame.importer;

import com.redblueflame.importer.visitor.ImporterClassVisitor;
import org.apache.commons.io.IOUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public abstract class ImporterTransformer extends DefaultTask {
    private File input;

    public ImporterTransformer() {
        try {
            this.mustRunAfter(this.getProject().getTasks().getByName("build"));
            input = getInputName();
            System.out.println(input.getPath());
        } catch (Exception e) {
            e.printStackTrace();
            input = null;
        }
    }

    @TaskAction
    public void apply() throws IOException {
        try {
            File file = input;
            System.out.println(file.getPath());
            processJar(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void processJar(File input) throws IOException {
        JarFile jar = new JarFile(input);
        // Enumerate over entries
        Enumeration<JarEntry> jarIter = jar.entries();
        while (jarIter.hasMoreElements()) {
            JarEntry jarEntry = jarIter.nextElement();
            // Only treat it if it finished by .class (bytecode file)
            if (jarEntry.getName().endsWith(".class")) {
                InputStream inputStream = jar.getInputStream(jarEntry);
                byte[] sourceClassBytes = IOUtils.toByteArray(inputStream);
                ImporterClassVisitor visitor = new ImporterClassVisitor(Opcodes.ASM4);
                ClassReader cr = new ClassReader(sourceClassBytes);
                cr.accept(visitor, 0);
                System.out.println(visitor.methodVisitor.getMethods());
            }
        }
    }

    private File getInputName() {
        return (File) getProject().getTasks().getByName("jar").getOutputs().getFiles().getFiles().toArray()[0];
    }
}
