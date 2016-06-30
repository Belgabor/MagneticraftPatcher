package mods.belgabor.mcpatcher;

import com.cout970.magneticraft.api.util.MgDirection;
import com.cout970.magneticraft.tileentity.TileInserter;
import mods.belgabor.mcpatcher.upgrades.ItemSneakyUpgrade;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Created by Belgabor on 23.06.2016.
 */
public class MCPatcherHooks {
    public static boolean crafterReplaceMatrix(IRecipe craftRecipe, World worldObj, InventoryCrafting craft, ItemStack result, ItemStack stack, int slot) {
        InventoryCrafting test = new InventoryCrafting(new Container() {
            @Override
            public boolean canInteractWith(EntityPlayer p_75145_1_) {
                return true;
            }
        }, 3, 3);
        for (int i = 0; i < 9; i++) {
            test.setInventorySlotContents(i, craft.getStackInSlot(i));
        }
        test.setInventorySlotContents(slot, stack);
        boolean ret = false;
        if (craftRecipe.matches(test, worldObj)) {
            ItemStack newResult = craftRecipe.getCraftingResult(test);
            if (result == null || result.getItem() == null) return false;
            ret = OreDictionary.itemMatches(result, newResult, true);
        }
        return ret;
    }
    
    public static MgDirection inserterGetAccessSide(TileInserter inserter, boolean from) {
        for (int l = 0; l < inserter.upgrades.getSizeInventory(); l++) {
            ItemStack stack = inserter.upgrades.getStackInSlot(l);
            if (stack != null && stack.getItem() instanceof ItemSneakyUpgrade) {
                ItemSneakyUpgrade item = (ItemSneakyUpgrade) stack.getItem();
                if (item.isFrom(stack) == from)
                    return item.getDirection(stack);
            }
        }
        return from?inserter.getDir().opposite():inserter.getDir();
    }
}
