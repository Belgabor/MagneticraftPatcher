package mods.belgabor.mcpatcher.upgrades;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * Created by Belgabor on 28.06.2016.
 */
public class SlotUpgrade extends Slot {
    public SlotUpgrade(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean isItemValid(ItemStack itemStack) {
        return (itemStack != null) && (itemStack.getItem() instanceof ItemUpgrade);
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }
}
