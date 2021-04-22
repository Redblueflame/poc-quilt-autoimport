package com.redblueflame.importer.visitor;

import com.redblueflame.importer.Log;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;

public class ImporterMethodVisitor extends MethodVisitor {
    private ArrayList<String> methods = new ArrayList<>();
    public ImporterMethodVisitor() {
        super(Opcodes.ASM4);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        addMethod(owner + "/" + name);
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        addMethod("dyn/" + name);
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
    }

    public ArrayList<String> getMethods() {
        return methods;
    }

    private void addMethod(String descriptor) {
        if (!methods.contains(descriptor)) {
            methods.add(descriptor);
        }
    }
}
