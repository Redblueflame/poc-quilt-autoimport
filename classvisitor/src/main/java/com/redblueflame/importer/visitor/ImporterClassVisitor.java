package com.redblueflame.importer.visitor;


import org.objectweb.asm.*;

public class ImporterClassVisitor extends ClassVisitor {
    public ImporterMethodVisitor methodVisitor = new ImporterMethodVisitor();
    private String className = "none";
    public ImporterClassVisitor(int api) {
        super(api);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        className = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        super.visitMethod(access, name, descriptor, signature, exceptions);
        methodVisitor.setFunction(className + "/" + name);
        return methodVisitor;
    }
}
