package mods.belgabor.mcpatcher;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;
import squeek.asmhelper.com.belgabor.mcpatcher.ASMHelper;
import squeek.asmhelper.com.belgabor.mcpatcher.ObfHelper;

import static org.objectweb.asm.Opcodes.*;

import java.util.Arrays;

/**
 * Created by Belgabor on 22.06.2016.
 */
public class MCPatcherTransformer implements IClassTransformer {
    
    private static final String[] classesBeingTransformed = {
            "com.cout970.magneticraft.util.InventoryComponent",
            "com.cout970.magneticraft.tileentity.shelf.TileShelf",
            "com.cout970.magneticraft.tileentity.shelf.TileShelfFiller"
    };
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] classBeingTransformed) {
        int index = Arrays.asList(classesBeingTransformed).indexOf(transformedName);
        return index != -1 ? transform(index, classBeingTransformed) : classBeingTransformed;
    }

    private byte[] transform(int index, byte[] classBeingTransformed) {
        System.out.println("Transforming: " + classesBeingTransformed[index]);
        try
        {
            ClassNode classNode = ASMHelper.readClassFromBytes(classBeingTransformed);

            switch(index)
            {
                case 0:
                    transformInventoryComponent(classNode);
                    break;
                case 1:
                    transformTileShelf(classNode);
                    break;
                case 2:
                    transformTileShelfFiller(classNode);
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

    private void transformTileShelfFiller(ClassNode classNode) {
        final String GET_MAIN_TILE = "getMainTile";
        final String GET_MAIN_TILE_DESC = "()Lcom/cout970/magneticraft/tileentity/shelf/TileShelvingUnit;";
        final String READ_NBT = ObfHelper.isObfuscated()?"func_145839_a":"readFromNBT";
        final String READ_NBT_DESC = "(Lnet/minecraft/nbt/NBTTagCompound;)V";
        final String WRITE_NBT = ObfHelper.isObfuscated()?"func_145841_b":"writeToNBT";
        final String WRITE_NBT_DESC = "(Lnet/minecraft/nbt/NBTTagCompound;)V";
        for (MethodNode method : classNode.methods) {
            if (method.name.equals(GET_MAIN_TILE) && method.desc.equals(GET_MAIN_TILE_DESC)) {
                System.out.println("Found/Patching TileShelfFiller:getMainTile");
                LabelNode newLabelNode = new LabelNode();

                InsnList toInsert = new InsnList();
                toInsert.add(new VarInsnNode(ALOAD, 0));
                toInsert.add(new FieldInsnNode(GETFIELD, "com/cout970/magneticraft/tileentity/shelf/TileShelfFiller", "offset", "Lcom/cout970/magneticraft/api/util/VecInt;"));
                toInsert.add(new JumpInsnNode(IFNONNULL, newLabelNode));
                toInsert.add(new InsnNode(ACONST_NULL));
                toInsert.add(new InsnNode(ARETURN));
                toInsert.add(newLabelNode);

                method.instructions.insertBefore(method.instructions.getFirst(), toInsert);
            } else if (method.name.equals(WRITE_NBT) && method.desc.equals(WRITE_NBT_DESC)) {
                System.out.println("Found TileShelfFiller:writeToNBT");
                AbstractInsnNode targetNode = ASMHelper.findFirstInstructionWithOpcode(method, INVOKESPECIAL);
                if (targetNode != null) {
                    System.out.println("Found TileShelfFiller:writeToNBT");
                    LabelNode newLabelNode = new LabelNode();

                    InsnList toInsert = new InsnList();
                    toInsert.add(new VarInsnNode(ALOAD, 0));
                    toInsert.add(new FieldInsnNode(GETFIELD, "com/cout970/magneticraft/tileentity/shelf/TileShelfFiller", "offset", "Lcom/cout970/magneticraft/api/util/VecInt;"));
                    toInsert.add(new JumpInsnNode(IFNONNULL, newLabelNode));
                    toInsert.add(new InsnNode(RETURN));
                    toInsert.add(newLabelNode);
                    
                    method.instructions.insert(targetNode, toInsert);
                }
            } else if (method.name.equals(READ_NBT) && method.desc.equals(READ_NBT_DESC)) {
                System.out.println("Found TileShelfFiller:readFromNBT");
                AbstractInsnNode targetNode = ASMHelper.findFirstInstructionWithOpcode(method, INVOKESPECIAL);
                if (targetNode != null) {
                    System.out.println("Found TileShelfFiller:readFromNBT");
                    LabelNode newLabelNode = new LabelNode();

                    InsnList toInsert = new InsnList();
                    toInsert.add(new VarInsnNode(ALOAD, 1));
                    toInsert.add(new LdcInsnNode("offsetX"));
                    toInsert.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/nbt/NBTTagCompound", ObfHelper.isObfuscated()?"func_74764_b":"hasKey", "(Ljava/lang/String;)Z", false));
                    toInsert.add(new JumpInsnNode(IFNE, newLabelNode));
                    toInsert.add(new InsnNode(RETURN));
                    toInsert.add(newLabelNode);

                    method.instructions.insert(targetNode, toInsert);
                }
            }
        }
    }

    private void transformTileShelf(ClassNode classNode) {
        final String GET_INVENTORY = "getInventory";
        final String GET_INVENTORY_DESC = "()Lcom/cout970/magneticraft/util/InventoryResizable;";
        for (MethodNode method : classNode.methods) {
            if (method.name.equals(GET_INVENTORY) && method.desc.equals(GET_INVENTORY_DESC)) {
                System.out.println("Found/Patching TileShelf:getInventory");
                LabelNode newLabelNode = new LabelNode();
                
                InsnList toInsert = new InsnList();
                toInsert.add(new VarInsnNode(ALOAD, 0));
                toInsert.add(new MethodInsnNode(INVOKEVIRTUAL, "com/cout970/magneticraft/tileentity/shelf/TileShelf", "getOffset", "()Lcom/cout970/magneticraft/api/util/VecInt;", false));
                toInsert.add(new JumpInsnNode(IFNONNULL, newLabelNode));
                toInsert.add(new InsnNode(ACONST_NULL));
                toInsert.add(new InsnNode(ARETURN));
                toInsert.add(newLabelNode);
                
                method.instructions.insertBefore(method.instructions.getFirst(), toInsert);
            }
        }        
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
