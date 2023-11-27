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

public class RenderItemTransformer implements ClassFileTransformer {

    private final ClassPool rootPool;
    private final ScopedClassPoolFactoryImpl scopedClassPoolFactory = new ScopedClassPoolFactoryImpl();
    private static final byte[] searchBytes = new byte[]{17, 63, 0, -128, 54};

    public RenderItemTransformer() {
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

            // RenderItem doesn't have nice string constants, instead we look for SIPUSH 0x3F00, which is only ever used in RenderItem
            if (ByteArraySearcher.contains(ctClass.toBytecode(), searchBytes)) {
                System.out.println("[NegRendererAgent] RenderItem searching class "+ctClass.getName());

                // we have to defrost the ctclass as we froze it reading the bytecode to see if we even needed to do anything
                ctClass.defrost();

                ClassFile classFile = ctClass.getClassFile();

                for (MethodInfo methodInfo : classFile.getMethods()) {
                    CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
                    CodeIterator codeIterator = codeAttribute.iterator();
                    byte[] methodByteCopy = new byte[codeIterator.getCodeLength()];

                    for (int index = 0; index < codeIterator.getCodeLength(); index++) {
                        methodByteCopy[index] = (byte)codeIterator.byteAt(index);
                    }

                    // as our method also contains the SIPUSH, we can just look for it again
                    if (ByteArraySearcher.contains(methodByteCopy, searchBytes) && methodByteCopy.length > 10) {
                        System.out.println("[NegRendererAgent] Found method!");

                        // as the compilation sometimes changes the "count > 1" check into a "count <= 1", we have to look for both

                        // opcode for <=
                        if (methodByteCopy[10] == -92) {
                            // count <= 1 don't render
                            // replace with count == 1 don't render

                            // opcode for ==
                            methodByteCopy[10] = (byte)-97;
                        }
                        // opcode for >
                        else if (methodByteCopy[10] == -93) {
                            // count > 1 do render
                            // replace with count != 1 do render

                            // opcode for !=
                            methodByteCopy[10] = (byte) -96;
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
