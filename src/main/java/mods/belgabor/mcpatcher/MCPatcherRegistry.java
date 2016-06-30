package mods.belgabor.mcpatcher;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import mods.belgabor.mcpatcher.upgrades.ItemSneakyUpgrade;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.ShapedOreRecipe;

/**
 * Created by Belgabor on 01.07.2016.
 */
public class MCPatcherRegistry {
    public static ItemSneakyUpgrade upgradeSneaky;
    public static boolean upgradeSneakyEnabled = true;
    
    public static void preInit(FMLPreInitializationEvent event) {
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        
        upgradeSneakyEnabled = config.getBoolean("upgradeSneakyEnabled", Configuration.CATEGORY_GENERAL, upgradeSneakyEnabled, "Enable the sneaky upgrade for the inserter");
        
        if (config.hasChanged())
            config.save();
        
        upgradeSneaky = new ItemSneakyUpgrade("sneakyupgrade");
        if (upgradeSneakyEnabled)
            GameRegistry.registerItem(upgradeSneaky, "sneakyupgrade");
    }
    
    public static void init() {
        if (upgradeSneakyEnabled)
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(upgradeSneaky), "ppp", "pip", "ppp", 'p', "sheetPlastic", 'i', Blocks.hopper));
    }
}
