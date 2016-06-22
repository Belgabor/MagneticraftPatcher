package mods.belgabor.mcpatcher;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import squeek.asmhelper.com.belgabor.mcpatcher.ASMHelper;
import squeek.asmhelper.com.belgabor.mcpatcher.ObfHelper;

import static org.objectweb.asm.Opcodes.*;

import java.util.Arrays;

/**
 * Created by Belgabor on 22.06.2016.
 */
public class MCPatcherTransformer implements IClassTransformer {
    
    private static final String[] classesBeingTransformed = {
            "com.cout970.magneticraft.util.InventoryComponent"
    };
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] classBeingTransformed) {
        int index = Arrays.asList(classesBeingTransformed).indexOf(transformedName);
        return index != -1 ? transform(index, classBeingTransformed) : classBeingTransformed;
    }

    private byte[] transform(int index, byte[] classBeingTransformed) {
        System.out.println("Transforming: " + classesBeingTransformed[index]);
        if (ObfHelper.isObfuscated())
            System.out.println("Obfuscated Environment");
        try
        {
            ClassNode classNode = ASMHelper.readClassFromBytes(classBeingTransformed);

            switch(index)
            {
                case 0:
                    transformInventoryComponent(classNode);
                    break;
            }

            return ASMHelper.writeClassToBytes(classNode, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return classBeingTransformed;
    }

    private void transformInventoryComponent(ClassNode classNode) {
        final String READ_NBT = "readFromNBT";
        final String READ_NBT_DESC = "(Lnet/minecraft/nbt/NBTTagCompound;Ljava/lang/String;)V";
        final String WRITE_NBT = "writeToNBT";
        final String WRITE_NBT_DESC = "(Lnet/minecraft/nbt/NBTTagCompound;Ljava/lang/String;)V";
        for (MethodNode method : classNode.methods) {
            if (method.name.equals(READ_NBT) && method.desc.equals(READ_NBT_DESC)) {
                System.out.println("Found InventoryComponent:readFromNBT");
                final AbstractInsnNode queryNode = new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/nbt/NBTTagCompound", ObfHelper.isObfuscated()?"func_74771_c":"getByte", "(Ljava/lang/String;)B", false);
                AbstractInsnNode targetNode = null;
                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (ASMHelper.instructionsMatch(instruction, queryNode)) {
                        targetNode = instruction;
                        break;
                    }
                }
                if (targetNode != null) {
                    System.out.println("Patching InventoryComponent:readFromNBT");
                    MethodInsnNode writeNode = (MethodInsnNode) targetNode;
                    writeNode.name = ObfHelper.isObfuscated()?"func_74762_e":"getInteger";
                    writeNode.desc = "(Ljava/lang/String;)I";
                }
                
            } else if (method.name.equals(WRITE_NBT) && method.desc.equals(WRITE_NBT_DESC)) {
                System.out.println("Found InventoryComponent:writeToNBT");
                AbstractInsnNode targetNode = ASMHelper.findFirstInstructionWithOpcode(method, I2B);
                if (targetNode != null && targetNode.getPrevious().getOpcode() == ILOAD && targetNode.getNext().getOpcode() == INVOKEVIRTUAL) {
                    System.out.println("Patching InventoryComponent:writeToNBT");
                    MethodInsnNode writeNode = (MethodInsnNode) targetNode.getNext();
                    method.instructions.remove(targetNode);
                    writeNode.name = ObfHelper.isObfuscated()?"func_74768_a":"setInteger";
                    writeNode.desc = "(Ljava/lang/String;I)V";
                }
            }
        }
    }
}
