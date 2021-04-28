package com.redblueflame.importer.visitor;


import org.objectweb.asm.*;

public class ImporterClassVisitor extends ClassVisitor {
    public ImporterMethodVisitor methodVisitor = new ImporterMethodVisitor();
    private String className = "none";

    public ImporterClassVisitor() {
        super(Opcodes.ASM4);
    }

    @Override
    public void visitSource(String source, String debug) {
        methodVisitor.setFile(source);
        super.visitSource(source, debug);
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
