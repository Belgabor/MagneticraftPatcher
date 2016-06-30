package mods.belgabor.mcpatcher;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import mods.belgabor.mcpatcher.upgrades.ItemSneakyUpgrade;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;


@Mod(modid = MCPatcherConstants.MOD_ID, version = MCPatcherConstants.MOD_VERSION, name = MCPatcherConstants.MOD_NAME, dependencies = "required-after:Magneticraft@[0.6.0,)")
public class MCPatcherMod
{
    public static ItemSneakyUpgrade upgradeSneaky;

    @Mod.EventHandler
    public void preInit (FMLPreInitializationEvent event) {
        upgradeSneaky = new ItemSneakyUpgrade("sneakyupgrade");
        GameRegistry.registerItem(upgradeSneaky, "sneakyupgrade");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(upgradeSneaky), "ppp", "pip", "ppp", 'p', "sheetPlastic", 'i', Blocks.hopper));
    }

}
