package com.redblueflame.importer.visitor;


import org.objectweb.asm.*;

public class ImporterClassVisitor extends ClassVisitor {
    public ImporterMethodVisitor methodVisitor = new ImporterMethodVisitor();
    public ImporterClassVisitor(int api) {
        super(api);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        super.visitMethod(access, name, descriptor, signature, exceptions);
        return methodVisitor;
    }
}