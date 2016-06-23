package mods.belgabor.mcpatcher;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;
import squeek.asmhelper.com.belgabor.mcpatcher.ASMHelper;
import squeek.asmhelper.com.belgabor.mcpatcher.ObfHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;

import static org.objectweb.asm.Opcodes.*;

/**
 * Created by Belgabor on 22.06.2016.
 */
public class MCPatcherTransformer implements IClassTransformer {
    
    private static final String[] classesBeingTransformed = {
            "com.cout970.magneticraft.util.InventoryComponent",
            "com.cout970.magneticraft.tileentity.shelf.TileShelf",
            "com.cout970.magneticraft.tileentity.shelf.TileShelfFiller",
            "com.cout970.magneticraft.util.InventoryCrafterAux",
            "com.cout970.magneticraft.util.InventoryCrafterAux$1"
    };
    private static final boolean debug = false;
    private static final boolean debugNoPatch = false;
    private static final File dumpDir = new File("dumps/");

    private static final String[] dumpClasses = {
    };
    
    {
        if (debug) {
            if (!dumpDir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                dumpDir.mkdirs();
            }
        }
    }
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] classBeingTransformed) {
        if (debug) {
            if (Arrays.asList(dumpClasses).indexOf(transformedName) >= 0) {
                dumpClass(transformedName, classBeingTransformed);
            }
        }
        if (debugNoPatch)
            return classBeingTransformed;
        int index = Arrays.asList(classesBeingTransformed).indexOf(transformedName);
        return index != -1 ? transform(index, classBeingTransformed) : classBeingTransformed;
    }

    private void dumpClass(String transformedName, byte[] classBeingTransformed) {
        ClassReader reader = new ClassReader(classBeingTransformed);
        try {
            reader.accept(new TraceClassVisitor(null, new Textifier(), new PrintWriter(new File(dumpDir, transformedName + ".text.txt"))), 0);
            reader.accept(new TraceClassVisitor(null, new ASMifier(), new PrintWriter(new File(dumpDir, transformedName + ".asm.txt"))), 0);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private byte[] transform(int index, byte[] classBeingTransformed) {
        MCPatcherLogger.info("Transforming: " + classesBeingTransformed[index]);
        try {
            ClassNode classNode = ASMHelper.readClassFromBytes(classBeingTransformed);

            switch(index) {
                case 0:
                    transformInventoryComponent(classNode);
                    break;
                case 1:
                    transformTileShelf(classNode);
                    break;
                case 2:
                    transformTileShelfFiller(classNode);
                    break;
                case 3:
                    transformInventoryCrafterAux(classNode);
                    break;
                case 4:
                    transformInventoryCrafterAuxContainer(classNode);
                    break;
            }

            return ASMHelper.writeClassToBytes(classNode, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        }
        catch (PatchFailed e) {
            MCPatcherLogger.error("FAILED: " + e.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return classBeingTransformed;
    }

    private void transformInventoryCrafterAuxContainer(ClassNode classNode) throws PatchFailed {
        final String CONSTRUCTOR = "<init>";
        final String CONSTRUCTOR_DESC = "()V";
        final String ON_CHANGED = ObfHelper.isObfuscated()?"func_75130_a":"onCraftMatrixChanged";
        final String ON_CHANGED_DESC = ObfHelper.desc("(Lnet/minecraft/inventory/IInventory;)V");
        classNode.fields.add(new FieldNode(ACC_FINAL + ACC_SYNTHETIC, "val$craft", "Lcom/cout970/magneticraft/tileentity/TileCrafter;", null, null));
        boolean doPatch = false;
        
        for (MethodNode method : classNode.methods) {
            if (method.name.equals(CONSTRUCTOR) && method.desc.equals(CONSTRUCTOR_DESC)) {
                MCPatcherLogger.info("Found InventoryCrafterAux$1:<init>");
                AbstractInsnNode targetNode = ASMHelper.findFirstInstructionWithOpcode(method, INVOKESPECIAL);

                if (targetNode != null) {
                    MCPatcherLogger.info("Patching InventoryCrafterAux$1:<init>");
                    doPatch = true;
                    method.desc = "(Lcom/cout970/magneticraft/tileentity/TileCrafter;)V";
                    method.visitMaxs(2, 2);

                    InsnList toInsert = new InsnList();
                    toInsert.add(new VarInsnNode(ALOAD, 1));
                    toInsert.add(new FieldInsnNode(PUTFIELD, "com/cout970/magneticraft/util/InventoryCrafterAux$1", "val$craft", "Lcom/cout970/magneticraft/tileentity/TileCrafter;"));
                    toInsert.add(new VarInsnNode(ALOAD, 0));
                    
                    method.instructions.insertBefore(targetNode, toInsert);
                } else
                    throw new PatchFailed("InventoryCrafterAux$1:<init> - Instruction not found");
            }
        }
        
        if (doPatch) {
            MethodNode onCraftMatrixChanged = new MethodNode(ACC_PUBLIC, ON_CHANGED, ON_CHANGED_DESC, null, null);
            onCraftMatrixChanged.visitMaxs(2, 2);
            InsnList toInsert = new InsnList();

            LabelNode startLabel = new LabelNode();
            LabelNode endLabel = new LabelNode();
            toInsert.add(startLabel);
            toInsert.add(new VarInsnNode(ALOAD, 0));
            toInsert.add(new VarInsnNode(ALOAD, 1));
            toInsert.add(new MethodInsnNode(INVOKESPECIAL, ObfHelper.getInternalClassName("net.minecraft.inventory.Container"), ON_CHANGED, ON_CHANGED_DESC, false));
            toInsert.add(new LabelNode());
            toInsert.add(new VarInsnNode(ALOAD, 0));
            toInsert.add(new FieldInsnNode(GETFIELD, "com/cout970/magneticraft/util/InventoryCrafterAux$1", "val$craft", "Lcom/cout970/magneticraft/tileentity/TileCrafter;"));
            toInsert.add(new MethodInsnNode(INVOKEVIRTUAL, "com/cout970/magneticraft/tileentity/TileCrafter", "refreshRecipe", "()V", false));
            toInsert.add(new LabelNode());
            toInsert.add(new InsnNode(RETURN));
            toInsert.add(endLabel);
            onCraftMatrixChanged.instructions.insert(toInsert);

            onCraftMatrixChanged.localVariables.add(new LocalVariableNode("this", "Lcom/cout970/magneticraft/util/InventoryCrafterAux$1;", null, startLabel, endLabel, 0));
            onCraftMatrixChanged.localVariables.add(new LocalVariableNode("p_75130_1_", ObfHelper.getDescriptor("net.minecraft.inventory.IInventory"), null, startLabel, endLabel, 1));
            
            classNode.methods.add(onCraftMatrixChanged);
        } else
            throw new PatchFailed("InventoryCrafterAux$1:<init> - Constructor not found");
    }

    private void transformInventoryCrafterAux(ClassNode classNode) throws PatchFailed {
        final String CONSTRUCTOR = "<init>";
        final String CONSTRUCTOR_DESC = "(Lcom/cout970/magneticraft/tileentity/TileCrafter;II)V";
        boolean patched = false;
        for (MethodNode method : classNode.methods) {
            if (method.name.equals(CONSTRUCTOR) && method.desc.equals(CONSTRUCTOR_DESC)) {
                MCPatcherLogger.info("Found InventoryCrafterAux:<init>");
                AbstractInsnNode targetNode = ASMHelper.findFirstInstructionWithOpcode(method, INVOKESPECIAL);
                
                if (targetNode != null && targetNode.getPrevious().getOpcode() == DUP) {
                    MCPatcherLogger.info("Patching InventoryCrafterAux:<init>");
                    patched = true;
                    method.instructions.insertBefore(targetNode, new VarInsnNode(ALOAD, 1));

                    MethodInsnNode writeNode = (MethodInsnNode) targetNode;
                    writeNode.desc = "(Lcom/cout970/magneticraft/tileentity/TileCrafter;)V";
                } else
                    throw new PatchFailed("InventoryCrafterAux:<init> - Instruction not found");
            }
        }
        if (!patched)
            throw new PatchFailed("InventoryCrafterAux:<init> - Constructor not found");
    }

    private void transformTileShelfFiller(ClassNode classNode) throws PatchFailed {
        final String GET_MAIN_TILE = "getMainTile";
        final String GET_MAIN_TILE_DESC = "()Lcom/cout970/magneticraft/tileentity/shelf/TileShelvingUnit;";
        final String READ_NBT = ObfHelper.isObfuscated()?"func_145839_a":"readFromNBT";
        final String READ_NBT_DESC = "(Lnet/minecraft/nbt/NBTTagCompound;)V";
        final String WRITE_NBT = ObfHelper.isObfuscated()?"func_145841_b":"writeToNBT";
        final String WRITE_NBT_DESC = "(Lnet/minecraft/nbt/NBTTagCompound;)V";
        int patches = 0;
        
        for (MethodNode method : classNode.methods) {
            if (method.name.equals(GET_MAIN_TILE) && method.desc.equals(GET_MAIN_TILE_DESC)) {
                MCPatcherLogger.info("Found/Patching TileShelfFiller:getMainTile");
                patches++;
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
                MCPatcherLogger.info("Found TileShelfFiller:writeToNBT");
                AbstractInsnNode targetNode = ASMHelper.findFirstInstructionWithOpcode(method, INVOKESPECIAL);
                if (targetNode != null) {
                    MCPatcherLogger.info("Patching TileShelfFiller:writeToNBT");
                    patches++;
                    LabelNode newLabelNode = new LabelNode();

                    InsnList toInsert = new InsnList();
                    toInsert.add(new VarInsnNode(ALOAD, 0));
                    toInsert.add(new FieldInsnNode(GETFIELD, "com/cout970/magneticraft/tileentity/shelf/TileShelfFiller", "offset", "Lcom/cout970/magneticraft/api/util/VecInt;"));
                    toInsert.add(new JumpInsnNode(IFNONNULL, newLabelNode));
                    toInsert.add(new InsnNode(RETURN));
                    toInsert.add(newLabelNode);
                    
                    method.instructions.insert(targetNode, toInsert);
                } else
                    throw new PatchFailed("TileShelfFiller:writeToNBT - Instruction not found");
            } else if (method.name.equals(READ_NBT) && method.desc.equals(READ_NBT_DESC)) {
                MCPatcherLogger.info("Found TileShelfFiller:readFromNBT");
                AbstractInsnNode targetNode = ASMHelper.findFirstInstructionWithOpcode(method, INVOKESPECIAL);
                if (targetNode != null) {
                    MCPatcherLogger.info("Patching TileShelfFiller:readFromNBT");
                    patches++;
                    LabelNode newLabelNode = new LabelNode();

                    InsnList toInsert = new InsnList();
                    toInsert.add(new VarInsnNode(ALOAD, 1));
                    toInsert.add(new LdcInsnNode("offsetX"));
                    toInsert.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/nbt/NBTTagCompound", ObfHelper.isObfuscated()?"func_74764_b":"hasKey", "(Ljava/lang/String;)Z", false));
                    toInsert.add(new JumpInsnNode(IFNE, newLabelNode));
                    toInsert.add(new InsnNode(RETURN));
                    toInsert.add(newLabelNode);

                    method.instructions.insert(targetNode, toInsert);
                } else
                    throw new PatchFailed("TileShelfFiller:readFromNBT - Instruction not found");
            }
        }
        if (patches != 3)
            throw new PatchFailed("TileShelfFiller - Some methods not found");
    }

    private void transformTileShelf(ClassNode classNode) throws PatchFailed {
        final String GET_INVENTORY = "getInventory";
        final String GET_INVENTORY_DESC = "()Lcom/cout970/magneticraft/util/InventoryResizable;";
        boolean patched = false;
        for (MethodNode method : classNode.methods) {
            if (method.name.equals(GET_INVENTORY) && method.desc.equals(GET_INVENTORY_DESC)) {
                MCPatcherLogger.info("Found/Patching TileShelf:getInventory");
                patched = true;
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
        if (!patched)
            throw new PatchFailed("TileShelf:getInventory - Method not found");
    }

    private void transformInventoryComponent(ClassNode classNode) throws PatchFailed {
        final String READ_NBT = "readFromNBT";
        final String READ_NBT_DESC = "(Lnet/minecraft/nbt/NBTTagCompound;Ljava/lang/String;)V";
        final String WRITE_NBT = "writeToNBT";
        final String WRITE_NBT_DESC = "(Lnet/minecraft/nbt/NBTTagCompound;Ljava/lang/String;)V";
        int patches = 0;
        for (MethodNode method : classNode.methods) {
            if (method.name.equals(READ_NBT) && method.desc.equals(READ_NBT_DESC)) {
                MCPatcherLogger.info("Found InventoryComponent:readFromNBT");
                final AbstractInsnNode queryNode = new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/nbt/NBTTagCompound", ObfHelper.isObfuscated()?"func_74771_c":"getByte", "(Ljava/lang/String;)B", false);
                AbstractInsnNode targetNode = null;
                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (ASMHelper.instructionsMatch(instruction, queryNode)) {
                        targetNode = instruction;
                        break;
                    }
                }
                if (targetNode != null) {
                    MCPatcherLogger.info("Patching InventoryComponent:readFromNBT");
                    patches++;
                    MethodInsnNode writeNode = (MethodInsnNode) targetNode;
                    writeNode.name = ObfHelper.isObfuscated()?"func_74762_e":"getInteger";
                    writeNode.desc = "(Ljava/lang/String;)I";
                } else
                    throw new PatchFailed("InventoryComponent:readFromNBT - Instruction not found");
            } else if (method.name.equals(WRITE_NBT) && method.desc.equals(WRITE_NBT_DESC)) {
                MCPatcherLogger.info("Found InventoryComponent:writeToNBT");
                AbstractInsnNode targetNode = ASMHelper.findFirstInstructionWithOpcode(method, I2B);
                if (targetNode != null && targetNode.getPrevious().getOpcode() == ILOAD && targetNode.getNext().getOpcode() == INVOKEVIRTUAL) {
                    MCPatcherLogger.info("Patching InventoryComponent:writeToNBT");
                    patches++;
                    MethodInsnNode writeNode = (MethodInsnNode) targetNode.getNext();
                    method.instructions.remove(targetNode);
                    writeNode.name = ObfHelper.isObfuscated()?"func_74768_a":"setInteger";
                    writeNode.desc = "(Ljava/lang/String;I)V";
                } else
                    throw new PatchFailed("InventoryComponent:writeToNBT - Instruction not found");
            }
        }
        if (patches != 2)
            throw new PatchFailed("InventoryComponent - Some methods not found");
    }
}
