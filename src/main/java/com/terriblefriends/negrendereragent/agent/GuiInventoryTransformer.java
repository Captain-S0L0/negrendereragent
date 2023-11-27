package com.terriblefriends.negrendereragent.agent;

import com.terriblefriends.negrendereragent.util.ByteArraySearcher;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.*;
import javassist.runtime.Desc;
import javassist.scopedpool.ScopedClassPoolFactoryImpl;
import javassist.scopedpool.ScopedClassPoolRepositoryImpl;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;

public class GuiInventoryTransformer implements ClassFileTransformer {

    private final ClassPool rootPool;
    private final ScopedClassPoolFactoryImpl scopedClassPoolFactory = new ScopedClassPoolFactoryImpl();

    public GuiInventoryTransformer() {
        Desc.useContextClassLoader = true;
        rootPool = ClassPool.getDefault();
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        byte[] returnBytecode = classfileBuffer;
        try {
            ClassPool classPool = scopedClassPoolFactory.create(loader, rootPool,
                    ScopedClassPoolRepositoryImpl.getInstance());
            CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));

            ClassFile classFile = ctClass.getClassFile();

            ConstPool constPool = classFile.getConstPool();

            boolean found = false;

            // only GuiInventory and InventoryEffectRender use this string constant for the texture
            for (int index = 0; index < constPool.getSize(); index++) {
                try {
                    if (constPool.getTag(index) == 1 && "/gui/inventory.png".equals(constPool.getUtf8Info(index))) {
                        found = true;
                        break;
                    }
                }
                catch (NullPointerException ignored) {

                }
            }

            if (found) {
                System.out.println("[NegRendererAgent] GuiInventory searching "+ctClass.getName());

                for (MethodInfo methodInfo : classFile.getMethods()) {
                    CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
                    CodeIterator codeIterator = codeAttribute.iterator();
                    byte[] methodByteCopy = new byte[codeIterator.getCodeLength()];

                    for (int index = 0; index < codeIterator.getCodeLength(); index++) {
                        methodByteCopy[index] = (byte)codeIterator.byteAt(index);
                    }

                    // bytecode for <= 1
                    if (ByteArraySearcher.contains(methodByteCopy, new byte[]{4, -92, 0}) && methodByteCopy.length > 213) {
                        System.out.println("[NegRendererAgent] Found method!");

                        // opcode for <=
                        if (methodByteCopy[213] == -92) {
                            // count <= 1 don't render
                            // replace with count == 1 don't render

                            // opcode for ==
                            methodByteCopy[213] = (byte)-97;
                        }
                        else {
                            System.out.println("[NegRendererAgent] Failed to find correct byte!");

                            System.out.println("method "+methodInfo.getName());
                            System.out.println(Arrays.toString(methodByteCopy));
                        }

                        methodInfo.setCodeAttribute(new CodeAttribute(codeAttribute.getConstPool(),codeAttribute.getMaxStack(), codeAttribute.getMaxLocals(), methodByteCopy, codeAttribute.getExceptionTable()));
                    }
                }

                returnBytecode = ctClass.toBytecode();
            }
            ctClass.detach();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return returnBytecode;
    }
}
