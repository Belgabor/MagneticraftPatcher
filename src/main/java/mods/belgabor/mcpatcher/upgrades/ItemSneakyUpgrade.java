package mods.belgabor.mcpatcher.upgrades;

import com.cout970.magneticraft.api.util.MgDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.belgabor.mcpatcher.MCPatcherLogger;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.lwjgl.input.Keyboard;

import java.util.List;

/**
 * Created by Belgabor on 29.06.2016.
 */
public class ItemSneakyUpgrade extends ItemUpgrade {
    @SideOnly(Side.CLIENT)
    private IIcon[] icon_direction = new IIcon[6];
    
    @SideOnly(Side.CLIENT)
    private IIcon[] icon_from = new IIcon[2];
    
    public ItemSneakyUpgrade(String unlocalizedName) {
        super(unlocalizedName);
        this.setTextureName("magneticraftpatcher:"+unlocalizedName);
    }
    
    private void ensureTag(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null)
            tag = new NBTTagCompound();
        if (!tag.hasKey("from"))
            tag.setBoolean("from", true);
        if (!tag.hasKey("direction"))
            tag.setByte("direction", (byte) 0);
        stack.setTagCompound(tag);
    }
    
    public byte getDirectionInt(ItemStack stack) {
        ensureTag(stack);
        return stack.getTagCompound().getByte("direction");
    }
    
    public MgDirection getDirection(ItemStack stack) {
        return MgDirection.getDirection(getDirectionInt(stack));
    }

    public void setDirectionInt(ItemStack stack, byte direction) {
        ensureTag(stack);
        stack.getTagCompound().setByte("direction", direction);
    }

    public boolean isFrom(ItemStack stack) {
        ensureTag(stack);
        return stack.getTagCompound().getBoolean("from");
    }
    
    public void setFrom(ItemStack stack, boolean from) {
        ensureTag(stack);
        stack.getTagCompound().setBoolean("from", from);
    }
    
    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            ensureTag(stack);
            NBTTagCompound tag = stack.getTagCompound();
            tag.setBoolean("from", !tag.getBoolean("from"));
        }
        return stack;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote && player.isSneaking()) {
            //byte direction = (byte) (player.isSneaking() ? MgDirection.getDirection(side).opposite().ordinal() : side);
            byte direction = (byte) (getDirectionInt(stack) == side ? MgDirection.getDirection(side).opposite().ordinal() : side);
            ensureTag(stack);
            stack.getTagCompound().setByte("direction", direction);
            //MCPatcherLogger.info("%d %d %d %f %f %f", x, y, z, hitX, hitY, hitZ);
        }
        return true;
    }

    
    
    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return String.format("sneakyupgrade_%s_%d", isFrom(stack)?"S":"T", (int) getDirectionInt(stack));
    }

    @Override
    public int getDamage(ItemStack stack) {
        return getDirectionInt(stack) + (isFrom(stack)?6:0);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean flag) {
        NBTTagCompound tag = stack.getTagCompound();
        boolean from = true;
        byte direction = (byte) 0;
        if (tag != null && tag.hasKey("from") && tag.hasKey("direction")) {
            from = tag.getBoolean("from");
            direction = tag.getByte("direction");
        }
        list.add(StatCollector.translateToLocalFormatted("tooltip.sneakyupgrade.inventory", StatCollector.translateToLocal(from?"inventory.source":"inventory.target")));
        list.add(StatCollector.translateToLocalFormatted("tooltip.sneakyupgrade.side", StatCollector.translateToLocal(String.format("side.%d", direction % 6))));
        if (Keyboard.isKeyDown(42)) {
            list.add(StatCollector.translateToLocal("tooltip.sneakyupgrade.info1"));
            list.add(StatCollector.translateToLocal("tooltip.sneakyupgrade.info2"));
            list.add(StatCollector.translateToLocal("tooltip.sneakyupgrade.info3"));
        } else {
            list.add(EnumChatFormatting.DARK_GRAY + StatCollector.translateToLocal("tooltip.pressshift"));
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister register) {
        for (int i = 0; i < 6; i++) {
            icon_direction[i] = register.registerIcon(String.format("magneticraftpatcher:sneakyupgrade_%d", i));
        }
        icon_from[0] = register.registerIcon("magneticraftpatcher:sneakyupgrade_tgt");
        icon_from[1] = register.registerIcon("magneticraftpatcher:sneakyupgrade_src");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean requiresMultipleRenderPasses() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIconFromDamageForRenderPass(int damage, int pass) {
        if (pass == 0) {
            return icon_direction[damage % 6];
        } else if (pass == 1) {
            return icon_from[(damage / 2) % 2];
        }
        return null;
    }
}
