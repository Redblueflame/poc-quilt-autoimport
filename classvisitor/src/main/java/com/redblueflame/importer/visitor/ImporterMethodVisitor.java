package com.redblueflame.importer.visitor;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;

public class ImporterMethodVisitor extends MethodVisitor {

    private final ArrayList<AccessRecord> accesses = new ArrayList<>();
    private String function = "";
    private String file = "";
    private int line = -1;

    public ImporterMethodVisitor() {
        super(Opcodes.ASM4);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        addMethod(owner, name, descriptor);
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        addField(owner, name, descriptor);
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        this.line = line;
        super.visitLineNumber(line, start);
    }

    public ArrayList<AccessRecord> getAccesses() {
        return accesses;
    }

    public void setFunction(String name) {
        this.function = name;
    }

    public void setFile(String file) {
        this.file = file;
    }

    private void addField(String owner, String name, String descriptor) {
        String identifier = owner + "." + name + ";" + descriptor;
        addToArray(new AccessRecord(file, function, line, owner + "." + name, identifier));

    }

    private void addModule(String owner, String method) {
        String identifier = "module/" + owner;
        addToArray(new AccessRecord(file, function, line, owner + "." + method, identifier).special());
    }

    private void addMethod(String owner, String name, String descriptor) {
        String identifier = owner + "." + name + descriptor;
        addToArray(new AccessRecord(file, function, line, owner + "." + name, identifier));
        addModule(owner, name);
    }

    private void addToArray(AccessRecord ar) {
        if (accesses.stream().noneMatch(accessRecord -> accessRecord.descriptor.equals(ar.descriptor))) {
            accesses.add(ar);
        }
    }
}
