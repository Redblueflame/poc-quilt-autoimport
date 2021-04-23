package com.redblueflame.importer.visitor;

import com.redblueflame.importer.Log;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;

public class ImporterMethodVisitor extends MethodVisitor {
    private final ArrayList<AccessRecord> accesses = new ArrayList<>();
    private String function = "";

    public ImporterMethodVisitor() {
        super(Opcodes.ASM4);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        addAccess(owner + "." + name + descriptor);
        // Add an access to the owner for module detection.
        addAccess("module/" + owner);
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        addAccess("dyn/" + name + descriptor);
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        addAccess(owner + "." + name + ";" + descriptor);
        // Add an access to the owner for module detection.
        addAccess("module/" + owner);
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    public ArrayList<AccessRecord> getAccesses() {
        return accesses;
    }

    public void setFunction(String name) {
        this.function = name;
    }

    private void addAccess(String descriptor) {
        if (accesses.stream().noneMatch(accessRecord -> accessRecord.method.equals(descriptor))) {
            accesses.add(new AccessRecord(function, descriptor));
        }
    }
}
